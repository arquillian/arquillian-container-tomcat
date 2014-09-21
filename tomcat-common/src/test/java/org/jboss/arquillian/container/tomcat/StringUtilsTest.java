package org.jboss.arquillian.container.tomcat;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsBlankForNull() {

        assertThat(StringUtils.isBlank(null)).isTrue();
    }

    @Test
    public void testIsBlankForEmptyString() {

        assertThat(StringUtils.isBlank("")).isTrue();
    }

    @Test
    public void testIsBlankForWhitespaceOnly() {

        assertThat(StringUtils.isBlank(" \t\r\n")).isTrue();
    }

    @Test
    public void testIsBlankForCharacters() {

        assertThat(StringUtils.isBlank(" testing ")).isFalse();
    }
}
