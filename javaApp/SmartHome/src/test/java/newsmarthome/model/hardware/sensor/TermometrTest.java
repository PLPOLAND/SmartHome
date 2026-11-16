package newsmarthome.model.hardware.sensor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import newsmarthome.i2c.MasterToSlaveConverter;

class TermometrTest {

    private static byte[] reply(String text) {
        byte[] reply = new byte[8];
        for (int i = 0; i < text.length(); i++) {
            reply[i] = (byte) text.charAt(i);
        }
        return reply;
    }

    @Test
    void parseTemperatureKeepsTheMinusSign() {
        assertEquals(-3.5f, Termometr.parseTemperature(reply("-3.50"), 8));
        assertEquals(21.19f, Termometr.parseTemperature(reply("21.19"), 8));
        assertNull(Termometr.parseTemperature(new byte[8], 8));
    }

    @Test
    void minAndMaxFollowTheReadings() {
        Termometr termometr = new Termometr();

        termometr.setTemperatura(-5f);
        termometr.setTemperatura(20f);
        termometr.setTemperatura(10f);

        assertEquals(10f, termometr.getTemperatura());
        assertEquals(-5f, termometr.getMinTemperatura());
        assertEquals(20f, termometr.getMaxTemperatura());
    }

    @Test
    void failedReadingKeepsTheLastValue() {
        Termometr termometr = new Termometr();
        termometr.slaveSender = mock(MasterToSlaveConverter.class);
        when(termometr.slaveSender.checkTemperature(any())).thenReturn(21.5f, (Float) null);

        termometr.update();
        termometr.update();

        assertEquals(21.5f, termometr.getTemperatura());
        assertEquals(21.5f, termometr.getMinTemperatura());
        assertEquals(21.5f, termometr.getMaxTemperatura());
    }
}
