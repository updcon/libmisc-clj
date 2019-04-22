package libmisc_clj;

import com.eaio.uuid.UUIDGen;

import java.util.UUID;

public class UUIDTools {

    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    private static long to100Ns(final long timestampInMs) {
        return (timestampInMs * 10000) + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
    }

    private static long toUUIDTime(final long timestampIn100Ns) {

        return (timestampIn100Ns << 32)
                | 0x0000000000001000L
                | (timestampIn100Ns & 0x0000FFFF00000000L) >>> 16
                | (timestampIn100Ns & 0xFFFF000000000000L) >>> 48;
    }

    public static UUID toUUID(final long timestamp) {
        final long timestampIn100Ns = to100Ns(timestamp);
        return new UUID(toUUIDTime(timestampIn100Ns), UUIDGen.getClockSeqAndNode());
    }
}
