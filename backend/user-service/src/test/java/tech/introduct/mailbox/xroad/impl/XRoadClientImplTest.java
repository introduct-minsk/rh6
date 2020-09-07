package tech.introduct.mailbox.xroad.impl;

import ee.riik.xtee.client.types.ee.riik.xtee.aar.producers.producer.aar.OigusedVastus;
import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.dto.UserDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XRoadClientImplTest {

    @Test
    void findUserNullTest() {
        var user = new XRoadClientImpl(null, null).findUser(null);
        assertNull(user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
    }

    @Test
    void filterOigusAlates() {
        OigusedVastus.Oigused.Oigus role = mock(OigusedVastus.Oigused.Oigus.class);

        when(role.getOigusAlates()).thenReturn(null);
        assertTrue(XRoadClientImpl.filterOigusAlates(role));

        when(role.getOigusAlates()).thenReturn(map(Instant.now().plusSeconds(60)));
        assertTrue(XRoadClientImpl.filterOigusAlates(role));

        when(role.getOigusAlates()).thenReturn(map(Instant.now().minusSeconds(60)));
        assertFalse(XRoadClientImpl.filterOigusAlates(role));
    }

    @Test
    void filterOigusKuni() {
        OigusedVastus.Oigused.Oigus role = mock(OigusedVastus.Oigused.Oigus.class);

        when(role.getOigusKuni()).thenReturn(null);
        assertTrue(XRoadClientImpl.filterOigusKuni(role));

        when(role.getOigusKuni()).thenReturn(map(Instant.now().plusSeconds(60)));
        assertFalse(XRoadClientImpl.filterOigusKuni(role));

        when(role.getOigusKuni()).thenReturn(map(Instant.now().minusSeconds(60)));
        assertTrue(XRoadClientImpl.filterOigusKuni(role));
    }

    private Calendar map(Instant instant) {
        return GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
    }
}
