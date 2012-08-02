package org.jboss.arquillian.container.tomcat;

import static junit.framework.Assert.*;

import java.util.List;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:trepel@redhat.com">Tomas Repel</a>
 *
 */
public class AdditionalJavaOptionsParserTest {

	@Test
	public void parseNull() {
		String additionalProperties = null;
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(0, props.size());
	}

	@Test
	public void parseEmpty() {
		String additionalProperties = "";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(0, props.size());
	}

	@Test
	public void parseOnlyWhiteSpaces() {
		String additionalProperties = "  \t\n\t   ";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(0, props.size());
	}

	@Test
	public void parseDelimitedByWhitespaces() {
		String additionalProperties = "p0 p1\tp2\np3  p2\t\tp5\n\np6";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(7, props.size());
	}

	@Test
	public void parseDelimitedByQuotes() {
		String additionalProperties = "p0 \"p1 with space\"\t\"p2\"\"p3\"\n\"p4\" p5";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(6, props.size());
	}

	@Test
	public void parseWindowsPaths() {
		String additionalProperties = "p1=C:\\MyApps\\myApp.exe \"p2=C:\\Program Files\\MyApp\\myApp.exe\"";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(2, props.size());
	}

	@Test
	public void parseIfQuoteIsInside() {
		String additionalProperties = "p0 p1Quote\"Is\"Inside \"p2\"";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(3, props.size());
	}

	@Test
	public void parseQuotesOnly() {
		String additionalProperties = "\"p0\"\"p1\"\"p3\"\"p4\"";
		List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

		assertNotNull(props);
		assertEquals(4, props.size());
	}
}
