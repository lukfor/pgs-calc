package genepi.riskscore.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import picocli.CommandLine;

public class CreatePanelCommandTest {

	@Test
	public void testCreatePanel() {
		String[] args = { "test-data/PGS000957.txt.gz", "test-data/PGS000899.txt.gz", "--out", "test.txt.gz" };
		int result = new CommandLine(new CreatePanelCommand()).execute(args);
		assertEquals(0, result);

	}

}
