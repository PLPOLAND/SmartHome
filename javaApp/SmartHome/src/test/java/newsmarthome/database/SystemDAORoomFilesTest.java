package newsmarthome.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import newsmarthome.model.Room;
import newsmarthome.model.hardware.HardwareFactory;

class SystemDAORoomFilesTest {

    @TempDir
    Path tempDir;

    private String location() {
        return tempDir.toString() + File.separator;
    }

    private void writeRoom(int id, String name) throws Exception {
        Files.write(tempDir.resolve(id + "_Room.json"),
                ("{\"name\":\"" + name + "\",\"devices\":[],\"sensors\":[],\"id\":" + id + "}")
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void roomsAfterAGapInIdsAreStillLoaded() throws Exception {
        writeRoom(0, "Brak");
        writeRoom(2, "Salon");
        writeRoom(11, "Kuchnia");

        SystemDAO dao = new SystemDAO(mock(HardwareFactory.class), location());

        assertEquals(3, dao.getRoomsArrayList().size());
        assertEquals(2, dao.getRoom("Salon").getID());
        assertEquals(11, dao.getRoom("Kuchnia").getID());
        // nowy pokój nie może dostać id istniejącego pliku
        assertEquals(12, dao.nextRoomId());
    }

    @Test
    void removedRoomDoesNotHideLaterRoomsAfterRestart() throws Exception {
        writeRoom(0, "Brak");
        writeRoom(1, "Salon");
        writeRoom(2, "Kuchnia");
        SystemDAO dao = new SystemDAO(mock(HardwareFactory.class), location());

        assertTrue(dao.removeRoom(dao.getRoom(1)));

        SystemDAO restarted = new SystemDAO(mock(HardwareFactory.class), location());
        assertNull(restarted.getRoom("Salon"));
        assertNotNull(restarted.getRoom("Kuchnia"));
        assertNotNull(restarted.getRoom("Brak"));
    }

    @Test
    void brokenRoomFileIsSkippedAndLeftUntouched() throws Exception {
        writeRoom(0, "Brak");
        // urządzenie nieznanego typu przerywa wczytywanie pokoju już po odczytaniu jego nazwy
        String broken = "{\"name\":\"Zepsuty\",\"devices\":[{\"typ\":\"NIEZNANY\"}],\"sensors\":[],\"id\":1}";
        Files.write(tempDir.resolve("1_Room.json"), broken.getBytes(StandardCharsets.UTF_8));
        writeRoom(2, "Kuchnia");

        SystemDAO dao = new SystemDAO(mock(HardwareFactory.class), location());

        assertNull(dao.getRoom("Zepsuty"));
        assertNotNull(dao.getRoom("Kuchnia"));
        assertTrue(dao.getDevices().isEmpty());
        assertEquals(broken, new String(Files.readAllBytes(tempDir.resolve("1_Room.json")), StandardCharsets.UTF_8));
    }

    @Test
    void renamedRoomIsSavedUnderItsId() throws Exception {
        writeRoom(0, "Brak");
        writeRoom(3, "Salon");
        SystemDAO dao = new SystemDAO(mock(HardwareFactory.class), location());
        Room room = dao.getRoom(3);

        dao.renameRoom(room, "Pokój dzienny");

        SystemDAO restarted = new SystemDAO(mock(HardwareFactory.class), location());
        assertEquals(3, restarted.getRoom("Pokój dzienny").getID());
        assertNull(restarted.getRoom("Salon"));
    }
}
