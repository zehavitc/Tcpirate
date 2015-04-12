package ADC.SignatureCenter.ExportSC;

import ADC.Utils.*;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.PrintStream;
import java.io.FileOutputStream;

import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 27/12/2004
 * Time: 09:23:45
 * To change this template use File | Settings | File Templates.
 */
public class ExportIbizaSC extends ExportSCImpl {

    private static final String PROPERTY_DICTIONARY_FILE = "DICTIONARY_FILE";
    private static final String PROPERTY_VERSIONS_FILE = "VERSIONS_FILE";
    private static final String PROPERTY_TS_FILE = "TIMESTAMP_FILE";
    private static final String PROPERTY_QUERY = "QUERY";
    private static final String PROPERTY_FP = "fp";

    private static final String QUERY_SYSTEMVERSIONS =
            "select pk Pk, system_id SystemId, version_string VersionName, rtrim(cast(version_id as char(10))) VersionId from tblSystemVersions SystemVersion";

    private static final String QUERY_SYSTEMS =
            "select pk Pk, system_name Name, 'N/A' Note from tblSystems System";

    private static final String QUERY_RISKLEVELS =
            "select attack_risk Id, risk_description \"Desc\" from tblRiskLevels RiskLevel";

    private static final String QUERY_LMH =
            "select id Id, lmh_desc \"Desc\" from tblLowMediumHigh LowMediumHigh";

    private static final String QUERY_COMPLEXITYLEVELS =
            "select complexity_id id, complexity_description description from tblComplexityLevels ComplexityLevel";

    private static final String QUERY_ATTACKS =
            "select attack_id, attack_name, attack_summary, attack_details, attack_class, attack_exploit, attack_complexity_id, attack_false_positives, attack_false_negatives, attack_date_published, attack_date_updated, 'N/A' \"attack_updatedby\", attack_risk, attack_ref_cve, attack_ref_bugtraq, attack_ref_cert,isnull(attack_ref_contributors,'N/A') \"attack_ref_contributors\", isnull(attack_ref_additional, 'N/A') \"attack_ref_additional\", 'N/A' \"note\" from tblAttacks Attack";

    private static final String QUERY_ATTACKCLASSES =
            "select attack_class_id, attack_class_description from tblAttackClasses AttackClass";

    private static final String QUERY_AFFECTEDSYSTEMS =
            "select pk Pk, attack_id AttackId, isnull(system_name_id, 0) SystemNameId, isnull(system_version_pos, 'dontcare') SystemVersionPos, isnull(system_version_id, 0) SystemVersionId from tblAffectedSystems AffectedSystem";

    private static final String QUERY_SERVICEPORTS =
            "select service_id, isnull(port, -1) \"port\", isnull(port_range_from, -1) \"port_range_from\", isnull(port_range_to, -1) \"port_range_to\" from tblServicePorts servicePort";

    private static final String QUERY_SERVICES =
            "select id, name, displayName display_name, isnull(description, 'N/A') \"description\", protocolID protocol_id, case isDynamic when 0 then 'false' when 1 then 'true' end is_dynamic, case isnull(isEncrypted, 0) when 0 then 'false' when 1 then 'true' end is_encrypted, case isProfiled when 0 then 'false' when 1 then 'true' end is_profiled, connection_timeout from tblService service";

    private static final String QUERY_PROTOCOLS =
            "select id, name, displayName display_name, description from tblProtocol protocol";

    private static final String QUERY_SIGNATURES =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tsig_id as [Signature!1!SigId!element],\n" +
            "\tsig_attacksuperclass as [Signature!1!attackId!element],\n" +
            "\tname as [Signature!1!Name!element],\n" +
            "\tsig_signature as [Signature!1!SigSignature!element],\n" +
            "\tcase sig_dirClient2Server when 0 then 'false' when 1 then 'true' end as [Signature!1!IsClient2Server!element],\n" +
            "\tcase sig_dirServer2Client when 0 then 'false' when 1 then 'true' end as [Signature!1!IsServer2Client!element],\n" +
            "\tsig_frequency as [Signature!1!Frequency!element],\n" +
            "\tsig_accuracy as [Signature!1!Accuracy!element],\n" +
            "\tsig_datepublished as [Signature!1!DatePublished!element],\n" +
            "\tsig_dateupdated as [Signature!1!DateUpdated!element],\n" +
            "\tsig_source_key as [Signature!1!SigSrcId!element],\n" +
            "\tsig_source_revision as [Signature!1!SigSrcRev!element],\n" +
            "\tcase sig_deleted when 0 then 'false' when 1 then 'true' end as [Signature!1!IsDeleted!element],\n" +
            "\tcase isnull(sig_caseSensitive, 0) when 0 then 'false' when 1 then 'true' end as [Signature!1!caseSensitive!element],\n" +
            "\tcase sig_isDecoded when 0 then 'false' when 1 then 'true' end as [Signature!1!decoded!element],\n" +
            "\tnull as [services!2!!element],\n" +
            "\tnull as [service!3!!element],\n" +
            "\tnull as [locations!4!!element],\n" +
            "\tnull as [locations!4!mask]\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 350 and (sig_maxVer is null or sig_maxver >= 350)\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 350 and (sig_maxVer is null or sig_maxver >= 350)\n" +
            "union all\n" +
            "select 3 as tag,\n" +
            "\t2 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tserviceId,\n" +
            "\tnull,\n" +
            "\tnull\t\t\n" +
            "\tfrom tblSignatures, tblSignatureService where signatureId = sig_id and sig_goes2product = 1 and sig_minVer <= 350 and (sig_maxVer is null or sig_maxver >= 350)\n" +
            "union all\n" +
            "select 4 as tag,\n" +
            "\t1 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tsig_location\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 350 and (sig_maxVer is null or sig_maxver >= 350)\n";

    private static final String QUERY_SIGNATURES_FP =
        "select 1 as tag,\n" +
            "\tnull as parent,\n" +
            "\tsig_id as [Signature!1!SigId!element],\n" +
            "\tsig_attacksuperclass as [Signature!1!attackId!element],\n" +
            "\tname as [Signature!1!Name!element],\n" +
            "\tsig_signature as [Signature!1!SigSignature!element],\n" +
            "\tcase sig_dirClient2Server when 0 then 'false' when 1 then 'true' end as [Signature!1!IsClient2Server!element],\n" +
            "\tcase sig_dirServer2Client when 0 then 'false' when 1 then 'true' end as [Signature!1!IsServer2Client!element],\n" +
            "\tsig_frequency as [Signature!1!Frequency!element],\n" +
            "\tsig_accuracy as [Signature!1!Accuracy!element],\n" +
            "\tsig_datepublished as [Signature!1!DatePublished!element],\n" +
            "\tsig_dateupdated as [Signature!1!DateUpdated!element],\n" +
            "\tsig_source_key as [Signature!1!SigSrcId!element],\n" +
            "\tsig_source_revision as [Signature!1!SigSrcRev!element],\n" +
            "\tcase sig_deleted when 0 then 'false' when 1 then 'true' end as [Signature!1!IsDeleted!element],\n" +
            "\tcase isnull(sig_caseSensitive, 0) when 0 then 'false' when 1 then 'true' end as [Signature!1!caseSensitive!element],\n" +
            "\tcase sig_isDecoded when 0 then 'false' when 1 then 'true' end as [Signature!1!decoded!element],\n" +
            "\tnull as [services!2!!element],\n" +
            "\tnull as [service!3!!element],\n" +
            "\tnull as [locations!4!!element],\n" +
            "\tnull as [locations!4!mask]\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 360 and (sig_maxVer is null or sig_maxver >= 360)\n" +
            "union all\n" +
            "select 2 as tag,\n" +
            "\t1 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 360 and (sig_maxVer is null or sig_maxver >= 360)\n" +
            "union all\n" +
            "select 3 as tag,\n" +
            "\t2 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tserviceId,\n" +
            "\tnull,\n" +
            "\tnull\t\t\n" +
            "\tfrom tblSignatures, tblSignatureService where signatureId = sig_id and sig_goes2product = 1 and sig_minVer <= 360 and (sig_maxVer is null or sig_maxver >= 360)\n" +
            "union all\n" +
            "select 4 as tag,\n" +
            "\t1 as parent,\n" +
            "\tsig_id,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\tnull,\n" +
            "\t'',\n" +
            "\tsig_location\n" +
            "\tfrom tblSignatures where sig_goes2product = 1 and sig_minVer <= 360 and (sig_maxVer is null or sig_maxver >= 360)\n";

    private static final String[] locations = {"stream", "url", "parameters", "headers", "query", "parsed-query", "non-normalized-url", "url-and-parameters", "response-content"};

    private String generateCreationDate() {
        long now = System.currentTimeMillis();

        String ts_file = getProperty(PROPERTY_TS_FILE);
        if (ts_file == null)
            ts_file = "timestamp.txt";

        SimpleDateFormat sd = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );

        String d_str = sd.format(new Date(now));

        long real_now;

        try {
            real_now = (sd.parse(d_str)).getTime();
            PrintStream ps = new PrintStream(new FileOutputStream(ts_file), true);
            ps.print(real_now);
            ps.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create timestamp file");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        return "<creationDate>" + d_str + "</creationDate><creationTimestamp>" + Long.toString(real_now) + "</creationTimestamp>";
    }

    private Element locationElement(int location) {
        StringBuffer sb = new StringBuffer(200);
        sb.append("<locations>");

        int mask = 1;

        for (int index = 0; index < locations.length; index++) {
            if ((location & mask) != 0) {
                String name = locations[index];
                sb.append("<location>");
                sb.append(name);
                sb.append("</location>");
            }
            mask <<= 1;
        }

        sb.append("</locations>");

        return XmlUtils.getInstance().stringToDOM(sb.toString());
    }

    private String exportSignaturesXML(StringBuffer sb) {
        XmlUtils xmlu = XmlUtils.getInstance();

        Element el = xmlu.stringToDOM(sb.toString());

        NodeList nodes = el.getElementsByTagName("Signature");

        for (int index = 0; index < nodes.getLength(); index++) {
            Element sig = (Element)nodes.item(index);
            NodeList nl = sig.getElementsByTagName("locations");
            Element loc = (Element)nl.item(0);
            int id = Integer.parseInt(loc.getAttribute("mask"));
            Element new_loc = locationElement(id);
            new_loc = (Element)loc.getOwnerDocument().importNode(new_loc, true);
            sig.replaceChild(new_loc, loc);
        }

        return xmlu.toXmlString(el);
    }

    public String export() {

        StringBuffer sb = new StringBuffer(1000000);

        sb.append("<signature-center>");

        sb.append(exportSimpleXML(QUERY_SYSTEMVERSIONS, "SystemVersions"));
        sb.append(exportSimpleXML(QUERY_SYSTEMS, "Systems"));
        sb.append(exportSimpleXML(QUERY_RISKLEVELS, "RiskLevels"));
        sb.append(exportSimpleXML(QUERY_LMH, "LowMediumHighs"));
        sb.append(exportSimpleXML(QUERY_COMPLEXITYLEVELS, "ComplexityLevels"));
        sb.append(exportSimpleXML(QUERY_ATTACKCLASSES, "AttackClasses"));
        sb.append(exportSimpleXML(QUERY_AFFECTEDSYSTEMS, "AffectedSystems"));
        sb.append(exportSimpleXML(QUERY_SERVICEPORTS, "servicePorts"));
        sb.append(exportSimpleXML(QUERY_SERVICES, "services"));
        sb.append(exportSimpleXML(QUERY_PROTOCOLS, "protocols"));
        sb.append(exportSimpleXML(QUERY_ATTACKS, "Attacks"));

        StringBuffer sb1 = null;
        if (getProperty(PROPERTY_FP) != null)
            sb1 = exportSimpleXML(QUERY_SIGNATURES_FP + " ORDER BY 3", "Signatures", true);
        else
            sb1 = exportSimpleXML(QUERY_SIGNATURES + " ORDER BY 3", "Signatures", true);

        sb.append(exportSignaturesXML(sb1));

        String tmp = getProperty(PROPERTY_DICTIONARY_FILE);
        if (tmp == null)
            tmp = "ibiza-dictionaries.xml";
        sb.append(addXmlFile(tmp));

        tmp = getProperty(PROPERTY_VERSIONS_FILE);
        if (tmp == null)
            tmp = "ibiza-versions.xml";
        sb.append(addXmlFile(tmp));

        sb.append(generateCreationDate());

        sb.append("</signature-center>");

        Element el = XmlUtils.getInstance().stringToDOM(sb.toString());
        return XmlUtils.getInstance().toXmlString(el);
    }

    public String versionString() {
        return "Ibiza";
    }

    public String exportForPatch() {
        StringBuffer sb = null;

        if (getProperty(PROPERTY_FP) != null)
            sb = exportSimpleXML(QUERY_SIGNATURES_FP + " and sig_id in (" + getProperty(PROPERTY_QUERY) + ") order by 3", "Signatures", true);
        else
            sb = exportSimpleXML(QUERY_SIGNATURES + " and sig_id in (" + getProperty(PROPERTY_QUERY) + ") order by 3", "Signatures", true);

        return exportSignaturesXML(sb);
    }
}
