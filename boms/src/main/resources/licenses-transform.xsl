<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8"/>

  <!-- Global constants -->
  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'"/>
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

  <xsl:variable name="apache_v2_name" select="'Apache Software License, Version 2.0'"/>
  <xsl:variable name="apache_v2_url" select="'http://www.apache.org/licenses/LICENSE-2.0.txt'"/>

  <xsl:variable name="epl_v1_name" select="'Eclipse Public License, Version 1.0'"/>
  <xsl:variable name="epl_v1_url" select="'http://www.eclipse.org/legal/epl-v10.html'"/>

  <xsl:variable name="lgpl_v21_name" select="'GNU Lesser General Public License, Version 2.1'"/>
  <xsl:variable name="lgpl_v21_url" select="'http://www.gnu.org/licenses/lgpl-2.1.html'"/>

  <xsl:variable name="lgpl_v30_name" select="'GNU Lesser General Public License, Version 3'"/>
  <xsl:variable name="lgpl_v30_url" select="'http://www.gnu.org/licenses/lgpl-3.0-standalone.html'"/>

  <xsl:variable name="bsd_2_name" select="'BSD 2-clause &quot;Simplified&quot; License'"/>
  <xsl:variable name="bsd_2_url" select="'http://www.opensource.org/licenses/BSD-2-Clause'"/>

  <xsl:variable name="bsd_3_name" select="'BSD 3-clause &quot;New&quot; or &quot;Revised&quot; License'"/>
  <xsl:variable name="bsd_3_url" select="'http://www.opensource.org/licenses/BSD-3-Clause'"/>

  <xsl:variable name="mit_name" select="'The MIT License'"/>
  <xsl:variable name="mit_url" select="'http://www.opensource.org/licenses/MIT'"/>

  <xsl:variable name="cddl_name" select="'Common Development and Distribution License 1.1'"/>
  <xsl:variable name="cddl_url" select="'https://glassfish.java.net/public/CDDL+GPL_1_1.html'"/>

  <xsl:variable name="edl_v1_name" select="'Eclipse Distribution License, Version 1.0'"/>
  <xsl:variable name="edl_v1_url" select="'http://www.eclipse.org/org/documents/edl-v10.php'"/>

  <xsl:variable name="cca_name" select="'Creative Commons Attribution 2.5'"/>
  <xsl:variable name="cca_url" select="'http://creativecommons.org/licenses/by/2.5/'"/>

  <xsl:variable name="cpl_name" select="'Common Public License'"/>
  <xsl:variable name="cpl_url" select="'http://www.opensource.org/licenses/cpl1.0.txt'"/>

  <xsl:variable name="mpl_v11_name" select="'Mozilla Public License 1.1'"/>
  <xsl:variable name="mpl_v11_url" select="'http://www.mozilla.org/MPL/MPL-1.1.html'"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="license">
    <xsl:choose>
      <!-- General license name/url fixes/alignment -->
      <!-- ASL2 -->
      <xsl:when test="contains(url/text(), 'www.apache.org/licenses/license-2.0') or starts-with(name/text(), 'AL2') or contains(name/text(), 'Apache License, Version 2.0') or contains(name/text(), 'Apache Software License, Version 2.0') or contains(url/text(), 'repository.jboss.org/licenses/apache-2.0') or contains(url/text(), 'www.opensource.org/licenses/apache2.0')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$apache_v2_name"/>
          <xsl:with-param name="url" select="$apache_v2_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- Eclipse -->
      <xsl:when test="contains(url/text(), 'www.eclipse.org/legal/epl-v10') or contains(url/text(), 'www.eclipse.org/org/documents/epl-v10')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$epl_v1_name"/>
          <xsl:with-param name="url" select="$epl_v1_url"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(url/text(), 'www.eclipse.org/org/documents/edl-v10.php')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$edl_v1_name"/>
          <xsl:with-param name="url" select="$edl_v1_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- LGPL -->
      <xsl:when test="contains(url/text(), 'www.gnu.org/licenses/lgpl-2.1') or contains(url/text(), 'repository.jboss.org/licenses/lgpl') or contains(url/text(), 'repository.jboss.com/licenses/lgpl') or (contains(name/text(), 'LGPL') and contains(name/text(), '2.1'))">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$lgpl_v21_name"/>
          <xsl:with-param name="url" select="$lgpl_v21_url"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(url/text(), 'www.gnu.org/licenses/lgpl-3.0')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$lgpl_v30_name"/>
          <xsl:with-param name="url" select="$lgpl_v30_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- BSD -->
      <xsl:when test="contains(name/text(), 'The 2-Clause BSD License')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$bsd_2_name"/>
          <xsl:with-param name="url" select="$bsd_2_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- MIT -->
      <xsl:when test="contains(url/text(), 'www.opensource.org/licenses/mit-license') or contains(url/text(), 'www.opensource.org/licenses/MIT') or contains(url/text(), 'jsoup.org/license')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$mit_name"/>
          <xsl:with-param name="url" select="$mit_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- CDDL -->
      <xsl:when test="contains(url/text(), 'glassfish.java.net/public/CDDL+GPL_1_1')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$cddl_name"/>
          <xsl:with-param name="url" select="$cddl_url"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains(url/text(), 'glassfish.dev.java.net/nonav/public/CDDL+GPL')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$cddl_name"/>
          <xsl:with-param name="url" select="$cddl_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- CCA -->
      <xsl:when test="contains(url/text(), 'creativecommons.org/licenses/by/2.5')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$cca_name"/>
          <xsl:with-param name="url" select="$cca_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- CPL -->
      <xsl:when test="contains(url/text(), 'www.opensource.org/licenses/cpl1.0')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$cpl_name"/>
          <xsl:with-param name="url" select="$cpl_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- Mozilla -->
      <xsl:when test="contains(url/text(), 'http://www.mozilla.org/MPL/MPL-1.1.html')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$mpl_v11_name"/>
          <xsl:with-param name="url" select="$mpl_v11_url"/>
        </xsl:call-template>
      </xsl:when>

      <!-- GAV-specific fixes -->
      <!-- ASM -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'asm' or contains(ancestor::dependency/groupId/text(), 'org.ow2.asm')">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="'The Asm BSD License'"/>
          <xsl:with-param name="url" select="'http://asm.ow2.org/license.html'"/>
        </xsl:call-template>
      </xsl:when>
      <!-- antlr -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'antlr'">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$bsd_3_name"/>
          <xsl:with-param name="url" select="$bsd_3_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- relaxngDatatype -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'com.github.relaxng' and ancestor::dependency/artifactId/text() = 'relaxngDatatype'">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="$bsd_3_name"/>
          <xsl:with-param name="url" select="$bsd_3_url"/>
        </xsl:call-template>
      </xsl:when>
      <!-- dom4j -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'dom4j' and ancestor::dependency/artifactId/text() = 'dom4j'">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="'The Dom4j License'"/>
          <xsl:with-param name="url" select="'https://raw.githubusercontent.com/dom4j/dom4j/master/LICENSE'"/>
        </xsl:call-template>
      </xsl:when>
      <!-- openjdk-orb -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'org.jboss.openjdk-orb' and ancestor::dependency/artifactId/text() = 'openjdk-orb'">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="'GNU General Public License, Version 2 with the Classpath Exception'"/>
          <xsl:with-param name="url" select="'http://repository.jboss.org/licenses/gpl-2.0-ce.txt'"/>
        </xsl:call-template>
      </xsl:when>
      <!-- jaxen -->
      <xsl:when test="ancestor::dependency/groupId/text() = 'jaxen' and ancestor::dependency/artifactId/text() = 'jaxen'">
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="'The Jaxen License'"/>
          <xsl:with-param name="url" select="'http://www.jaxen.org/license.html'"/>
        </xsl:call-template>
      </xsl:when>

      <!-- If nothing matches, leave original values -->
      <xsl:otherwise>
        <xsl:call-template name="license">
          <xsl:with-param name="name" select="name/text()"/>
          <xsl:with-param name="url" select="url/text()"/>
          <xsl:with-param name="origin" select="'Original'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="license">
    <xsl:param name="name"/>
    <xsl:param name="url"/>
    <xsl:param name="origin" select="'Fixed'"/>
    <license>
      <!-- For debug purposes only -->
      <!-- xsl:comment><xsl:value-of select="$origin"/></xsl:comment-->
      <name><xsl:value-of select="$name"/></name>
      <url><xsl:value-of select="$url"/></url>
    </license>
  </xsl:template>

</xsl:stylesheet>