package genepi.riskscore.tasks.report;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

public class PercentageFormatFunction implements Mustache.Lambda {

	public void execute(Template.Fragment frag, Writer out) throws IOException {
		String number = frag.execute();
		double percentage = Double.parseDouble(number) * 100;
		DecimalFormat df = new DecimalFormat("###.##'%'");
		String result = df.format(percentage);
		out.write(result);
	}

}
