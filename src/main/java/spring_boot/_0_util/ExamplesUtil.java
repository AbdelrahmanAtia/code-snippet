package spring_boot._0_util;

public class ExamplesUtil {

	/**
	 * Creates a message of size @msgSize in KB.
	 */
	public static String createDataSize(int msgSize) {
		// Java chars are 2 bytes
		msgSize = msgSize / 2;
		msgSize = msgSize * 1024;
		StringBuilder sb = new StringBuilder(msgSize);
		for (int i = 0; i < msgSize; i++) {
			sb.append('a');
		}
		return sb.toString();
	}
}
