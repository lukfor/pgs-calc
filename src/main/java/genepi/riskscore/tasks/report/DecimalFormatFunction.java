package genepi.riskscore.tasks.report;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

public class DecimalFormatFunction implements Mustache.Lambda {

	private DecimalFormat formatter = new DecimalFormat("###,###,###");

	public void execute(Template.Fragment frag, Writer out) throws IOException {
		String number = frag.execute();
		try {
			Integer integer = Integer.parseInt(number);
			String result = formatter.format(integer);
			out.write(result);
		} catch (Exception e) {
			Double _double = Double.parseDouble(number);
			String result = formatter.format(_double);
			out.write(result);
		}

	}

}
