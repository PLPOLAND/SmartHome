package newsmarthome.i2c;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import newsmarthome.exception.HardwareException;

class MasterToSlaveConverterTest {

    private static final int SLAVE = 15;
    private static final byte[] W = { 'W' };
    private static final byte[] G = { 'G' };

    private I2CHardware hardware;
    private MasterToSlaveConverter converter;

    @BeforeEach
    void setUp() {
        hardware = mock(I2CHardware.class);
        converter = new MasterToSlaveConverter();
        converter.atmega = hardware;
        converter.eventResponseDelayMs = 1;
    }

    @Test
    void readEventsFromSlave_readsEveryPendingEventUnderBusLock() throws HardwareException {
        byte[] click = { 'C', 1, 2, 'C', 0, 0, 0, 0 };
        byte[] hold = { 'C', 2, 0, 'P', 0, 0, 0, 0 };
        when(hardware.transaction(eq(SLAVE), aryEq(W), anyLong(), anyInt())).thenReturn(new byte[] { 2, 0, 0, 0, 0, 0, 0, 0 });
        when(hardware.transaction(eq(SLAVE), aryEq(G), anyLong(), anyInt())).thenReturn(click, hold);

        List<byte[]> events = converter.readEventsFromSlave(SLAVE);

        assertEquals(2, events.size());
        assertArrayEquals(click, events.get(0));
        assertArrayEquals(hold, events.get(1));

        InOrder inOrder = inOrder(hardware);
        inOrder.verify(hardware).lockBus();
        inOrder.verify(hardware).transaction(eq(SLAVE), aryEq(W), eq(1L), eq(8));
        inOrder.verify(hardware, times(2)).transaction(eq(SLAVE), aryEq(G), eq(1L), eq(8));
        inOrder.verify(hardware).unlockBus();
    }

    @Test
    void readEventsFromSlave_doesNotSendGetWhenQueueIsEmpty() throws HardwareException {
        when(hardware.transaction(eq(SLAVE), aryEq(W), anyLong(), anyInt())).thenReturn(new byte[8]);

        assertTrue(converter.readEventsFromSlave(SLAVE).isEmpty());

        verify(hardware, never()).transaction(eq(SLAVE), aryEq(G), anyLong(), anyInt());
        verify(hardware).unlockBus();
    }

    @Test
    void readEventsFromSlave_skipsFramesThatAreNotEvents() throws HardwareException {
        byte[] error = { 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E' };
        byte[] click = { 'C', 1, 1, 'C', 0, 0, 0, 0 };
        when(hardware.transaction(eq(SLAVE), aryEq(W), anyLong(), anyInt())).thenReturn(new byte[] { 2, 0, 0, 0, 0, 0, 0, 0 });
        when(hardware.transaction(eq(SLAVE), aryEq(G), anyLong(), anyInt())).thenReturn(error, click);

        List<byte[]> events = converter.readEventsFromSlave(SLAVE);

        assertEquals(1, events.size());
        assertArrayEquals(click, events.get(0));
    }

    @Test
    void readEventsFromSlave_releasesBusLockOnError() throws HardwareException {
        when(hardware.transaction(eq(SLAVE), aryEq(W), anyLong(), anyInt())).thenThrow(new HardwareException("IO"));

        assertThrows(HardwareException.class, () -> converter.readEventsFromSlave(SLAVE));

        verify(hardware).lockBus();
        verify(hardware).unlockBus();
    }
}
