package tests.broskiclan.libotre;

import org.broskiclan.libotre.Libotre;
import org.broskiclan.libotre.log.Logger;
import org.junit.Test;

public class LoggerTesting {

	@Test
	public void testLogger() {
		var l = new Logger();
		l.info("Test!");
		System.out.println();
		Libotre.printBuild();
		Libotre.printLicenseHeader();
	}

}
