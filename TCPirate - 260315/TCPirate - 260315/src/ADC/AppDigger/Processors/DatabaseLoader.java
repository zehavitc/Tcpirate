package ADC.AppDigger.Processors;

import ADC.AppDigger.EventProcessor;
import ADC.AppDigger.EventProcessorImpl;
import ADC.Utils.DBHelper;
import ADC.Utils.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.PrintStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by IntelliJ IDEA.
 * User: amichai
 * Date: 18/04/2005
 * Time: 21:53:13
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseLoader extends EventProcessorImpl {

    private static final int LIST_PARAMS = 1;
    private static final int LIST_REQUESTCOOKIES = 2;
    private static final int LIST_REQUESTHEADERS = 3;
    private static final int LIST_RESPONSECOOKIES = 4;
    private static final int LIST_RESPONSEHEADERS = 5;

    private static final String QUERY_INSERTEVENT = "INSERT INTO HttpEvents ([id], [arrival_time], [server_group], [source_ip], [source_port], [server_ip], [server_port], [web_application], [method], [full_url], [response_code], [session_id], [anomaly_mask1], [response_time], [total_params], [total_request_cookies], [total_request_headers], [total_response_cookies], [total_response_headers], [host], [anomaly_mask2], [close_reason])\n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String QUERY_INSERTREQPARAM = "INSERT INTO HttpRequestParams ([event_id], [param_name], [param_value], [origin], [char_type], [local_size], [binding_state])\n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String QUERY_INSERTREQHEADER = "INSERT INTO HttpRequestHeaders ([event_id], [header_name], [header_value])\n" +
            "VALUES (?, ?, ?)";

    private static final String QUERY_INSERTREQCOOKIE = "INSERT INTO HttpRequestCookies ([event_id], [cookie_name], [cookie_value], [traceability])\n" +
            "VALUES (?, ?, ?, ?)";

    private static final String QUERY_INSERTREPLYHEADER = "INSERT INTO HttpResponseHeaders ([event_id], [header_name], [header_value])\n" +
            "VALUES (?, ?, ?)";

    private static final String QUERY_INSERTREPLYCOOKIE = "INSERT INTO HttpResponseCookies ([event_id], [cookie_name], [cookie_value], [path], [cookie_domain], [expiration], [persistent], [secure])\n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String QUERY_INSERTXML = "insert into HttpRequestExtra (event_id, source_file) values (?, ?)";

    private static final int MAX_STRING_SIZE = 8000;
    private static final int MAX_INDEX_SIZE = 800;
    private static final String STRING_TRUNCATED = "**********TRUNCATED**********";

    private StringBuffer m_buffer = null;

    private PreparedStatement stmtInsertEvent = null;
    private PreparedStatement stmtInsertReqCookie = null;
    private PreparedStatement stmtInsertReplyCookie = null;
    private PreparedStatement stmtInsertReqHeader = null;
    private PreparedStatement stmtInsertReplyHeader = null;
    private PreparedStatement stmtInsertReqParam = null;
    private PreparedStatement stmtInsertXML = null;

    private String m_source_name = null;

    private Connection conn = null;

    private void getElementValue(Element e, StringBuffer sb) {
        sb.setLength(0);

        Node n = e.getFirstChild();

        while (n != null) {
            if (n.getNodeType() == Node.TEXT_NODE)
                sb.append(n.getNodeValue());

            n = n.getNextSibling();
        }
    }

    private void handleHttpSession(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertEvent.setNull(12, java.sql.Types.INTEGER);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("id".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() != 0)
                            stmtInsertEvent.setString(12, m_buffer.toString());
                        else
                            stmtInsertEvent.setNull(12, java.sql.Types.VARCHAR);
                    }
                }

                n = n.getNextSibling();
            }
        } catch (Exception ex) {
            System.err.println("Failed to read session information");
            throw new RuntimeException(ex);
        }
    }

    private void handleRequestHeader(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertReqHeader.setLong(1, eid);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("name".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqHeader.setNull(2, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertReqHeader.setString(2, m_buffer.toString());
                        else
                            stmtInsertReqParam.setString(2, STRING_TRUNCATED);
                    } else if ("value".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqHeader.setNull(3, java.sql.Types.LONGVARCHAR);
                        else
                            stmtInsertReqHeader.setCharacterStream(3, new StringReader(m_buffer.toString()), m_buffer.length());
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertReqHeader.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Failed to handle request header");
            throw new RuntimeException(ex);
        }
    }

    private void handleResponseHeader(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertReplyHeader.setLong(1, eid);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("name".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyHeader.setNull(2, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReplyHeader.setString(2, m_buffer.toString());
                    } else if ("value".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyHeader.setNull(3, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReplyHeader.setString(3, m_buffer.toString());
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertReplyHeader.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Failed to handle response header");
            throw new RuntimeException(ex);
        }
    }

    private void handleRequestCookie(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertReqCookie.setLong(1, eid);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("name".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqCookie.setNull(2, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertReqCookie.setString(2, m_buffer.toString());
                        else
                            stmtInsertReqParam.setString(2, STRING_TRUNCATED);
                    } else if ("value".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqCookie.setNull(3, java.sql.Types.LONGVARCHAR);
                        else
                            stmtInsertReqCookie.setCharacterStream(3, new StringReader(m_buffer.toString()), m_buffer.length());
                    } else if ("traceability-state".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqCookie.setNull(4, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReqCookie.setString(4, m_buffer.toString());
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertReqCookie.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Failed to handle request cookie");
            throw new RuntimeException(ex);
        }
    }

    private void handleResponseCookie(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertReplyCookie.setLong(1, eid);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("name".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyCookie.setNull(2, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReplyCookie.setString(2, m_buffer.toString());
                    } else if ("value".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyCookie.setNull(3, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_STRING_SIZE)
                            stmtInsertReplyCookie.setString(3, m_buffer.toString());
                        else
                            stmtInsertReqParam.setString(3, STRING_TRUNCATED);
                    } else if ("path".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyCookie.setNull(4, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReplyCookie.setString(4, m_buffer.toString());
                    } else if ("domain".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReplyCookie.setNull(5, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReplyCookie.setString(5, m_buffer.toString());
                    } else if ("expiration".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertReplyCookie.setLong(6, Long.parseLong(m_buffer.toString()));
                    } else if ("persistent".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if ("true".equalsIgnoreCase(m_buffer.toString()))
                            stmtInsertReplyCookie.setBoolean(7, true);
                        else
                            stmtInsertReplyCookie.setBoolean(7, false);
                    } else if ("secure".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if ("true".equalsIgnoreCase(m_buffer.toString()))
                            stmtInsertReplyCookie.setBoolean(8, true);
                        else
                            stmtInsertReplyCookie.setBoolean(8, false);
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertReplyCookie.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Failed to handle response cookie");
            throw new RuntimeException(ex);
        }
    }

    private void handleRequestParam(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            stmtInsertReqParam.setLong(1, eid);

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("name".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqParam.setNull(2, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertReqParam.setString(2, m_buffer.toString());
                        else
                            stmtInsertReqParam.setString(2, STRING_TRUNCATED);
                    } else if ("value".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertReqParam.setNull(3, java.sql.Types.LONGVARCHAR);
                        else
                            stmtInsertReqParam.setCharacterStream(3, new StringReader(m_buffer.toString()), m_buffer.length());
                    } else if ("origin".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertReqParam.setInt(4, Integer.parseInt(m_buffer.toString()));
                    } else if ("char-type".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertReqParam.setLong(5, Long.parseLong(m_buffer.toString()));
                    } else if ("locale-size".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertReqParam.setInt(6, Integer.parseInt(m_buffer.toString()));
                    } else if ("binding".equalsIgnoreCase(element_name)) {
                        String state = ((Element)n).getAttribute("classification");
                        if (state == null)
                            stmtInsertReqParam.setNull(7, java.sql.Types.VARCHAR);
                        else
                            stmtInsertReqParam.setString(7, state);
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertReqParam.executeUpdate();
        } catch (Exception ex) {
            System.err.println("Failed to read parameter");
            throw new RuntimeException(ex);
        }
    }

    private void handleList(long eid, Element el, int type) {
        Node n = el.getFirstChild();
        int counter = 0;

        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (type) {
                    case LIST_PARAMS:
                        handleRequestParam(eid, (Element)n);
                        break;
                    case LIST_REQUESTHEADERS:
                        handleRequestHeader(eid, (Element)n);
                        break;
                    case LIST_REQUESTCOOKIES:
                        handleRequestCookie(eid, (Element)n);
                        break;
                    case LIST_RESPONSEHEADERS:
                        handleResponseHeader(eid, (Element)n);
                        break;
                    case LIST_RESPONSECOOKIES:
                        handleResponseCookie(eid, (Element)n);
                        break;
                }

                counter++;

            }

            n = n.getNextSibling();
        }

        try {
            stmtInsertEvent.setInt(14 + type, counter);

            if ((counter == 0) && (type == LIST_RESPONSEHEADERS))
                System.out.println("no headers " + eid);

        } catch (Exception ex) {
            System.err.println("Failed to handle counter: " + type);
            throw new RuntimeException(ex);
        }
    }

    private void handleURL(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("full-url".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertEvent.setNull(10, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertEvent.setString(10, m_buffer.toString());
                        else
                            stmtInsertEvent.setString(10, STRING_TRUNCATED);
                    } else if ("host".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertEvent.setNull(20, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertEvent.setString(20, m_buffer.toString());
                        else
                            stmtInsertEvent.setString(20, STRING_TRUNCATED);
                    } else if ("method".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertEvent.setNull(9, java.sql.Types.VARCHAR);
                        else if (m_buffer.length() < MAX_INDEX_SIZE)
                            stmtInsertEvent.setString(9, m_buffer.toString());
                        else
                            stmtInsertEvent.setString(9, STRING_TRUNCATED);
                    } else if ("query-string".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        if (m_buffer.length() == 0)
                            stmtInsertXML.setNull(2, java.sql.Types.VARCHAR);
                        else
                            stmtInsertXML.setCharacterStream(2, new StringReader(m_buffer.toString()), m_buffer.length());
                    }
                }

                n = n.getNextSibling();
            }
        } catch (Exception ex) {
            System.err.println("Failed to handle URL information");
            throw new RuntimeException(ex);
        }
    }

    private void handleHttpRequest(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("cookies".equalsIgnoreCase(element_name)) {
                        handleList(eid, (Element)n, LIST_REQUESTCOOKIES);
                    } else if ("headers".equalsIgnoreCase(element_name)) {
                        handleList(eid, (Element)n, LIST_REQUESTHEADERS);
                    } else if ("param-items".equalsIgnoreCase(element_name)) {
                        handleList(eid, (Element)n, LIST_PARAMS);
                    } else if ("url".equalsIgnoreCase(element_name)) {
                        handleURL(eid, (Element)n);
                    }
                }

                n = n.getNextSibling();
            }
        } catch (Exception ex) {
            System.err.println("Failed to read request data");
            throw new RuntimeException(ex);
        }
    }

    private void handleHttpResponse(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("set-cookies".equalsIgnoreCase(element_name)) {
                        handleList(eid, (Element)n, LIST_RESPONSECOOKIES);
                    } else if ("headers".equalsIgnoreCase(element_name)) {
                        handleList(eid, (Element)n, LIST_RESPONSEHEADERS);
                    } else if ("response-code".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        try {
                            int code = Integer.parseInt(m_buffer.toString());
                            stmtInsertEvent.setInt(11, code);
                        } catch (Exception ex) {
                            stmtInsertEvent.setNull(11, java.sql.Types.INTEGER);
                        }
                    }
                }

                n = n.getNextSibling();
            }
        } catch (Exception ex) {
            System.err.println("Failed to read request data");
            throw new RuntimeException(ex);
        }
    }

    private void handleHttpStruct(long eid, Element el) {
        Node n = el.getFirstChild();

        try {
            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();

                    if ("session".equalsIgnoreCase(element_name)) {
                        handleHttpSession(eid, (Element)n);
                    } else if ("http-request".equalsIgnoreCase(element_name)) {
                        handleHttpRequest(eid, (Element)n);
                    } else if ("http-response".equalsIgnoreCase(element_name)) {
                        handleHttpResponse(eid, (Element)n);
                    }
                }

                n = n.getNextSibling();
            }
        } catch (Exception ex) {
            System.err.println("Failed to read Http Data");
            throw new RuntimeException(ex);
        }
    }

    private long IPtoNumber(String ip) {
        long result = 0;

        int last_pos = 0;
        int pos = ip.indexOf('.');

        result = Integer.parseInt(ip.substring(last_pos, pos));

        last_pos = pos + 1;
        pos = ip.indexOf('.', last_pos);

        result <<= 8;
        result += Integer.parseInt(ip.substring(last_pos, pos));

        last_pos = pos + 1;
        pos = ip.indexOf('.', last_pos);

        result <<= 8;
        result += Integer.parseInt(ip.substring(last_pos, pos));

        last_pos = pos + 1;

        result <<= 8;
        result += Integer.parseInt(ip.substring(last_pos));

        return result;
    }

    private void handleHttpEvent(Element el) {

        long eid = Long.parseLong(el.getAttribute("id"));
        // Main data of event

        Node n = el.getFirstChild();

        try {
            stmtInsertEvent.setLong(1, eid);
            stmtInsertEvent.setLong(3, Long.parseLong(el.getAttribute("server-group-id")));
            stmtInsertEvent.setString(22, el.getAttribute("close-reason"));

            while (n != null) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    String element_name = n.getNodeName();
                    if ("arrival-time".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertEvent.setLong(2, Long.parseLong(m_buffer.toString()));
                    } else if ("source-ip".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertEvent.setLong(4, IPtoNumber(m_buffer.toString()));
                    } else if ("source-port".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertEvent.setInt(5, Integer.parseInt(m_buffer.toString()));
                    } else if ("server-ip".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertEvent.setLong(6, IPtoNumber(m_buffer.toString()));
                    } else if ("server-port".equalsIgnoreCase(element_name)) {
                        getElementValue((Element)n, m_buffer);
                        stmtInsertEvent.setInt(7, Integer.parseInt(m_buffer.toString()));
                    } else if ("irregulars".equalsIgnoreCase(element_name)) {
                        String mask = ((Element)n).getAttribute("mask");
                        if (mask != null) {
                            stmtInsertEvent.setLong(13, Long.parseLong(mask.substring(0,32), 2));
                            stmtInsertEvent.setLong(21, Long.parseLong(mask.substring(32), 2));
                        } else {
                            stmtInsertEvent.setLong(13, 0);
                            stmtInsertEvent.setLong(21, 0);
                        }
                    } else if ("http-struct".equalsIgnoreCase(element_name)) {
                        String tmp = ((Element)n).getAttribute("response-time");
                        if (!"N/A".equalsIgnoreCase(tmp))
                            stmtInsertEvent.setInt(14, Integer.parseInt(((Element)n).getAttribute("response-time")));
                        else
                            stmtInsertEvent.setNull(14, java.sql.Types.INTEGER);

                        stmtInsertEvent.setString(8, ((Element)n).getAttribute("web-application-name"));

                        handleHttpStruct(eid, (Element)n);
                    }
                }

                n = n.getNextSibling();
            }

            stmtInsertEvent.executeUpdate();

            stmtInsertXML.setLong(1, eid);
            stmtInsertXML.setString(2, m_source_name);

            stmtInsertXML.executeUpdate();

            // stmtInsertXML.setCharacterStream(3, new StringReader(XmlUtils.getInstance().toXmlString(el)), 1);
            // stmtInsertXML.executeUpdate();

            conn.commit();

        } catch (Exception ex) {
            System.err.println(XmlUtils.getInstance().toXmlString(el));
            System.err.println("Failed to process event in file " + m_source_name);

            try {
                conn.rollback();
            } catch (Exception ex1) {
                throw new RuntimeException(ex1);
            }

            throw new RuntimeException(ex);
        }
    }

    public int handleEvent(Element e) {
        if (e.getAttribute("event-type").equalsIgnoreCase("http"))
            handleHttpEvent(e);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleStartSession(PrintStream p_output, PrintStream p_error) {

        DBHelper dbh = DBHelper.getInstance();

        String db_host = getProperty("dbhost");
        if (db_host == null)
            db_host = "127.0.0.1";

        String db_port_str = getProperty("dbport");
        if (db_port_str == null)
            db_port_str = "1433";

        String db_name = getProperty("dbname");
        if (db_name == null)
            db_name = "Research";

        String db_user = getProperty("dbuser");
        if (db_user == null)
            db_user = "research";

        String db_password = getProperty("dbpass");
        if (db_password == null)
            db_password = "research";

        dbh.connect(db_host, db_port_str, db_name, db_user, db_password, true);

        conn = dbh.getDedicatedConnection();

        try {
            conn.setAutoCommit(false);

            stmtInsertEvent = conn.prepareStatement(QUERY_INSERTEVENT);
            stmtInsertReqParam = conn.prepareStatement(QUERY_INSERTREQPARAM);
            stmtInsertReqHeader = conn.prepareStatement(QUERY_INSERTREQHEADER);
            stmtInsertReqCookie = conn.prepareStatement(QUERY_INSERTREQCOOKIE);
            stmtInsertReplyHeader = conn.prepareStatement(QUERY_INSERTREPLYHEADER);
            stmtInsertReplyCookie = conn.prepareStatement(QUERY_INSERTREPLYCOOKIE);
            stmtInsertXML = conn.prepareStatement(QUERY_INSERTXML);
        } catch (Exception ex) {
            System.err.println("Failed to initialize database access");
            throw new RuntimeException(ex);
        }

        m_buffer = new StringBuffer(10000);

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleEndSession() {
        try {
            stmtInsertEvent.close();
            stmtInsertReqParam.close();
            stmtInsertReqHeader.close();
            stmtInsertReqCookie.close();
            stmtInsertReplyHeader.close();
            stmtInsertReplyCookie.close();

            conn.close();
        } catch (Exception ex) {
            System.err.println("Failed to release database resources");
            throw new RuntimeException(ex);
        }

        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleOpenSource(String source_name) {
        m_source_name = source_name;
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int handleCloseSource(String source_name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static public EventProcessor getNewInstance(String[] argv) {
        return new DatabaseLoader();
    }

    static public String getName() {
        return "Database Loader";
    }

    public static void main(String[] argv) {
        System.out.println(Long.parseLong("100000000000000000000000000000000000000000000000000000000000000", 2));
    }
}
