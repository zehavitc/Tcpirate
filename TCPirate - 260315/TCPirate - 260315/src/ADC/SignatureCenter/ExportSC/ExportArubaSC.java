package ADC.SignatureCenter.ExportSC;

import ADC.Utils.XmlUtils;

import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

import org.w3c.dom.*;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 27/12/2004
 * Time: 09:23:45
 * To change this template use File | Settings | File Templates.
 */
public class ExportArubaSC extends ExportSCImpl {

    private static final String PROPERTY_DICTIONARY_FILE = "DICTIONARY_FILE";
    private static final String PROPERTY_TS_FILE = "TIMESTAMP_FILE";
    private static final String PROPERTY_QUERY = "QUERY";

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
            "select attack_id, attack_name, attack_summary, attack_details, attack_class, attack_exploit, attack_complexity_id, attack_false_positives, attack_false_negatives, attack_date_published, attack_date_updated, 'N/A' \"attack_updatedby\", attack_risk, attack_ref_cve, attack_ref_bugtraq, attack_ref_cert, isnull(attack_ref_contributors, 'N/A') \"attack_ref_contributors\", isnull(attack_ref_additional, 'N/A') \"attack_ref_additional\", 'N/A' \"note\" from tblAttacks Attack";

    private static final String QUERY_ATTACKCLASSES =
            "select attack_class_id, attack_class_description from tblAttackClasses AttackClass";

    private static final String QUERY_AFFECTEDSYSTEMS =
            "select pk Pk, attack_id AttackId, isnull(system_name_id, 0) SystemNameId, isnull(system_version_pos, 'dontcare') SystemVersionPos, isnull(system_version_id, 0) SystemVersionId from tblAffectedSystems AffectedSystem";

    private static final String QUERY_SERVICEPORTS =
            "select 1 id, service_id, isnull(port, -1) \"port\", isnull(port_range_from, -1) \"port_range_from\", isnull(port_range_to, -1) \"port_range_to\" from tblServicePorts servicePort";

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
            "\t'false' as [Signature!1!IsBinary!element],\n" +
            "\tcase sig_isregexp when 0 then 'false' when 1 then 'true' end as [Signature!1!IsRegExpr!element],\n" +
            "\tcase sig_dirClient2Server when 0 then 'false' when 1 then 'true' end as [Signature!1!IsClient2Server!element],\n" +
            "\tcase sig_dirServer2Client when 0 then 'false' when 1 then 'true' end as [Signature!1!IsServer2Client!element],\n" +
            "\tsig_frequency as [Signature!1!Frequency!element],\n" +
            "\tsig_accuracy as [Signature!1!Accuracy!element],\n" +
            "\tsig_datepublished as [Signature!1!DatePublished!element],\n" +
            "\tsig_dateupdated as [Signature!1!DateUpdated!element],\n" +
            "\t'N/A' as [Signature!1!UpdateBy!element],\n" +
            "\t'N/A' as [Signature!1!Note!element],\n" +
            "\tsig_source_key as [Signature!1!SigSrcId!element],\n" +
            "\tsig_source_revision as [Signature!1!SigSrcRev!element],\n" +
            "\tcase sig_deleted when 0 then 'false' when 1 then 'true' end as [Signature!1!IsDeleted!element],\n" +
            "\tcase sig_isapplayer when 0 then 'false' when 1 then 'true' end as [Signature!1!applicationLayer!element],\n" +
            "\tcase isnull(sig_caseSensitive, 0) when 0 then 'false' when 1 then 'true' end as [Signature!1!caseSensitive!element],\n" +
            "\tcase sig_isDecoded when 0 then 'false' when 1 then 'true' end as [Signature!1!decoded!element],\n" +
            "\tdbo.getServices(sig_id) as [Signature!1!services!element],\n" +
            "\tcase sig_isUrl when 0 then 'false' when 1 then 'true' end as [Signature!1!IsLocURI!element],\n" +
            "\tcase sig_isParam when 0 then 'false' when 1 then 'true' end as [Signature!1!IsLocParam!element],\n" +
            "\tcase sig_isRaw when 0 then 'false' when 1 then 'true' end as [Signature!1!IsLocGeneral!element]\n" +
            "\tfrom tblSignatures where sig_minVer <= 300 and (sig_maxVer is null or sig_maxVer = 330)\n";

    private void fixXMLDates(Element el) {
        String tag_name = el.getTagName();

        if ((tag_name != null) &&
                ((tag_name.equalsIgnoreCase("attack_date_published")) ||
                    (tag_name.equalsIgnoreCase("attack_date_updated")) ||
                    (tag_name.equalsIgnoreCase("DatePublished")) ||
                    (tag_name.equalsIgnoreCase("DateUpdated")))) {
            NodeList nodes = el.getChildNodes();
            for (int index = 0; index < nodes.getLength(); index++) {
                Node n = nodes.item(index);
                if (n.getNodeValue() != null) {
                    SimpleDateFormat sd = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
                    try {
                        Date d = sd.parse(n.getNodeValue());
                        n.setNodeValue(Long.toString(d.getTime()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {

            NodeList nodes = el.getChildNodes();
            for (int index = 0; index < nodes.getLength(); index++) {
                Node n = nodes.item(index);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                    fixXMLDates((Element)n);
            }
        }
    }

    private StringBuffer fixDates(StringBuffer inp) {
        XmlUtils xmlu = XmlUtils.getInstance();

        Element root = xmlu.stringToDOM(inp.toString());
        fixXMLDates(root);
        return new StringBuffer(xmlu.toXmlString(root));
    }

    private String generateCreationDate() {
        long now = System.currentTimeMillis();

        String ts_file = getProperty(PROPERTY_TS_FILE);
        if (ts_file == null)
            ts_file = "timestamp.txt";

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(ts_file), true);
            ps.print(now);
            ps.close();
        } catch (Exception e) {
            System.err.println(this.getClass().getName() + ": Failed to create timestamp file");
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }

        return "<creationDate>" + now + "</creationDate>";
    }

    public String export() {
        StringBuffer sb = new StringBuffer(1000000);

        sb.append("<root>");

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
        sb.append(exportSimpleXML(QUERY_SIGNATURES + " and sig_goes2product = 1 order by sig_id", "Signatures", true));

        String tmp = getProperty(PROPERTY_DICTIONARY_FILE);
        if (tmp == null)
            tmp = "aruba-dictionaries.xml";
        sb.append(addXmlFile(tmp));

        sb.append(generateCreationDate());

        sb.append("</root>");

        sb = fixDates(sb);

        return sb.toString();
    }

    public String exportForPatch() {
        StringBuffer sb = exportSimpleXML(QUERY_SIGNATURES + " and sig_id in (" + getProperty(PROPERTY_QUERY) + ") order by sig_id", "Signatures", true);
        sb = fixDates(sb);

        return sb.toString();
    }

    public String versionString() {
        return "Aruba/3.0";
    }
}
