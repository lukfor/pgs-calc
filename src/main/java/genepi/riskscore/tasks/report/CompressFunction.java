package genepi.riskscore.tasks.report;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import blazing.chain.LZSEncoding;

public class CompressFunction implements Mustache.Lambda {

	public void execute(Template.Fragment frag, Writer out) throws IOException {
		String text = frag.execute();
		out.write(LZSEncoding.compressToBase64(text));
	}

}
