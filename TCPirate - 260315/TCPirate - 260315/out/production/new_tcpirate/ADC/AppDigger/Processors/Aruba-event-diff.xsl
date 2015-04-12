<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"/>
	<xsl:template match="/event[@type='http-req']">
	<event>
			<sourceip>
				<xsl:value-of select="sourceip"/>
			</sourceip>
			<sourceport>
				<xsl:value-of select="sourceport"/>
			</sourceport>
			<servername>
				<xsl:value-of select="servername"/>
			</servername>
			<server_port>
				<xsl:value-of select="server_port"/>
			</server_port>
			<method>
				<xsl:value-of select="http-request/url/method"/>
			</method>
			<full_url>
				<xsl:value-of select="http-request/url/full_url"/>
			</full_url>
			<query-string>
				<xsl:value-of select="http-request/url/query-string"/>
			</query-string>
			<host>
				<xsl:value-of select="http-request/url/host"/>
			</host>
			<headers>
				<xsl:for-each select="http-request/headers/header">
					<xsl:sort select="name"/>
					<header>
						<name>
							<xsl:value-of select="name"/>
						</name>
						<value>
							<xsl:value-of select="value"/>
						</value>

					</header>
				</xsl:for-each>
			</headers>
			<param-items>
				<xsl:for-each select="http-request/param-items/param-item">
					<xsl:sort select="name"/>
					<param-item>
						<name>
							<xsl:value-of select="name"/>
						</name>
						<value>
							<xsl:value-of select="value"/>
						</value>
						<origin>
							<xsl:value-of select="origin"/>
						</origin>
					</param-item>
				</xsl:for-each>
			</param-items>
	</event>
	</xsl:template>
	<xsl:template match="/event[@event-type='network']">
	<event>
			<source-ip>
				<xsl:value-of select="source-ip"/>
			</source-ip>
			<source-port>
				<xsl:value-of select="source-port"/>
			</source-port>
			<server-ip>
				<xsl:value-of select="server-ip"/>
			</server-ip>
			<server-port>
				<xsl:value-of select="server-port"/>
			</server-port>
			<raw-data><xsl:value-of select="network-struct/raw-data"></xsl:value-of></raw-data>
	</event>
	</xsl:template>

</xsl:stylesheet>
