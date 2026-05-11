package io.github.javamrcp.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RtpPortAllocatorTest {
    @Test
    void allocatesEvenPortsAndReusesReleasedPorts() {
        RtpPortAllocator allocator = new RtpPortAllocator(4001, 4006);

        assertEquals(4002, allocator.allocate());
        assertEquals(4004, allocator.allocate());
        assertEquals(2, allocator.allocatedCount());

        allocator.release(4002);
        assertEquals(1, allocator.allocatedCount());
        assertEquals(4002, allocator.allocate());
    }

    @Test
    void throwsWhenRangeExhausted() {
        RtpPortAllocator allocator = new RtpPortAllocator(4000, 4000);
        assertEquals(4000, allocator.allocate());
        assertThrows(IllegalStateException.class, allocator::allocate);
    }
}
