/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.RFXComValueSelector.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING4;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting4Message.Commands.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting4Message.SubType.PT2262;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfigurationBuilder;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComLighting4MessageTest {
    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComLighting4Message message = (RFXComLighting4Message) RFXComMessageFactory.createMessage(LIGHTING4);

        RFXComDeviceConfiguration build = new RFXComDeviceConfigurationBuilder().withDeviceId("90000").withPulse(300)
                .withSubType("PT2262").build();
        message.setConfig(build);
        message.convertFromState(COMMAND, OnOffType.ON);

        byte[] binaryMessage = message.decodeMessage();
        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactory.createMessage(binaryMessage);

        assertEquals("Sensor Id", "90000", msg.getDeviceId());

    }

    private void testMessage(String hexMsg, RFXComLighting4Message.SubType subType, String deviceId, Integer pulse,
            RFXComLighting4Message.Commands command, Integer seqNbr, int signalLevel, int offCommand, int onCommand)
            throws RFXComException {
        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactory
                .createMessage(DatatypeConverter.parseHexBinary(hexMsg));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Command", command.toByte(), RFXComTestHelper.getActualIntValue(msg, COMMAND_ID));
        if (seqNbr != null) {
            assertEquals("Seq Number", seqNbr.shortValue(), (short) (msg.seqNbr & 0xFF));
        }
        assertEquals("Signal Level", signalLevel, RFXComTestHelper.getActualIntValue(msg, SIGNAL_LEVEL));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, DatatypeConverter.printHexBinary(decoded));

        RFXComTestHelper.checkDiscoveryResult(msg, deviceId, pulse, subType.toString(), offCommand, onCommand);
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("091300E1D8AD59018F70", PT2262, "887509", 399, ON_9, 225, 7, 4, 9);
        testMessage("0913005FA9A9C901A170", PT2262, "694940", 417, ON_9, 95, 7, 4, 9);
        testMessage("091300021D155C01E960", PT2262, "119125", 489, ON_12, 2, 6, 4, 12);
        testMessage("091300D345DD99018C50", PT2262, "286169", 396, ON_9, 211, 5, 4, 9);
        testMessage("09130035D149A2017750", PT2262, "857242", 375, OFF_2, 53, 5, 2, 1);
    }

    @Test
    public void testSomeConradMessages() throws RFXComException {
        testMessage("0913003554545401A150", PT2262, "345413", 417, OFF_4, 53, 5, 4, 1);
    }

    @Test
    public void testPhenixMessages() throws RFXComException {
        List<String> onMessages = Arrays.asList("09130046044551013780", "09130048044551013780", "0913004A044551013980",
                "0913004C044551013780", "0913004E044551013780");

        for (String message : onMessages) {
            testMessage(message, PT2262, "17493", null, ON_1, null, 8, 4, 1);
        }

        List<String> offMessages = Arrays.asList("09130051044554013980", "09130053044554013680", "09130055044554013680",
                "09130057044554013680", "09130059044554013680", "0913005B044554013680", "0913005D044554013480",
                "09130060044554013980", "09130062044554013680", "09130064044554013280");

        for (String message : offMessages) {
            testMessage(message, PT2262, "17493", null, OFF_4, null, 8, 4, 1);
        }
    }
}
