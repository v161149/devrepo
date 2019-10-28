package com.verizon.vprice.errorhandling.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import com.verizon.vprice.util.VPriceProp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.apache.commons.io.serialization.ValidatingObjectInputStream;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import oracle.jdbc.OraclePreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Date;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.logging.Level;

public class CommonUtil {
	
    public static final String EMPTY_STRING = "NA";
    public static final String VPRICE_APP = "Pricing Engine";
    public static final String VPRICE_APP_SCHEMA = "VPRICE_APP";
    public static final String NOTIFICATION_IND_YES = "Y";
    public static final String INITIAL_ERR_STATUS = "NEW";
    public static final String NOTIFICATION_TEMPLATE_ID = "VPE_ERR_01";
    public static final String NOTIFICATION_ELM_DATETIME = "DATE_TIME";
    public static final String NOTIFICATION_ELM_APPNAME = "APP_NAME";
    public static final String NOTIFICATION_ELM_ENVADDR = "ENV_ADDR";
    public static final String NOTIFICATION_ELM_ORIGSYS = "ORIG_SYS";
    public static final String NOTIFICATION_ELM_SUBSYS = "SUB_SYS";
    public static final String NOTIFICATION_ELM_QUOTE = "QUOTE_ID";
    public static final String NOTIFICATION_ELM_SCEN = "SCEN_ID";
    public static final String NOTIFICATION_ELM_USER = "USER_ID";
    public static final String NOTIFICATION_ELM_STATUS = "APP_STATUS";
    public static final String NOTIFICATION_ELM_DESC = "APP_DESC";
    public static final String NOTIFICATION_ELM_INPUT = "ORIG_INPUT";
    public static final String NOTIFICATION_ELM_ERR = "ERR_DESC";
    public static final String NOTIFICATION_ELM_ERRSTACK = "ERR_STACK";   
    public static final String NOTIFICATION_SUBJECT = "vPrice Exception Report";
    private static HashMap<String,String> emailTemplates = null;
    private static final Object accesslock= new Object();
    private static final String envName = System.getProperty("weblogic.Name"); 
    
    private static DataSource getVpriceDataSource() {
        Context env = null;
        DataSource pool = null;
        Hashtable ht = new Hashtable();
        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        try {
            env = new InitialContext(ht);
            pool = (DataSource)env.lookup("jdbc/VPRICEDS");
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
        return pool;
    }

    public static Connection getVpriceDBConnection() throws Exception {
        DataSource datasource = getVpriceDataSource();
        Connection conn = datasource.getConnection();
        return conn;
    }
    
    public static void persistErrorMessage(VpriceErrorMsgDTO errMsgData) throws Exception {
        String PERSIST_ERR_MSG = "INSERT INTO " + VPRICE_APP_SCHEMA + ".VPE_ERROR_QUEUE(MESSAGE_TYPE,DESCRIPTION,ORIG_SYSTEM,SUB_SYSTEM,QUOTE_ID,SCEN_ID,USER_ID,STATUS,INPUT_DATA,OUTPUT_DATA,ERROR_DESC,ERROR_STACK,ISSUE_STATUS,IS_MAIL_REQ,EMAIL_TEMPLATE_ID,WORKED_ON_USER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection vPriceConn = null; 
        PreparedStatement statement = null;
        String sqlStmt = null;
        String emailSubject = EMPTY_STRING;
        String emailBody = EMPTY_STRING;
        
        try {
            if (errMsgData!=null) {
                errMsgData.setIssueStatus(INITIAL_ERR_STATUS);
                errMsgData.setEmailTemplateId(NOTIFICATION_TEMPLATE_ID);
                errMsgData.setWorkedOnUser(EMPTY_STRING);
                vPriceConn = getVpriceDBConnection();
                sqlStmt = PERSIST_ERR_MSG;
                statement = vPriceConn.prepareStatement(sqlStmt);
                ((OraclePreparedStatement)statement).setString(1, nullSafeToString(errMsgData.getRequestType(),100));
                ((OraclePreparedStatement)statement).setString(2, nullSafeToString(errMsgData.getDescription(),500));
                ((OraclePreparedStatement)statement).setString(3, nullSafeToString(errMsgData.getOrigSystem(),100)); 
                ((OraclePreparedStatement)statement).setString(4, nullSafeToString(errMsgData.getSubSystem(),100));
                ((OraclePreparedStatement)statement).setString(5, nullSafeToString(errMsgData.getQuoteId(),50));
                ((OraclePreparedStatement)statement).setString(6, nullSafeToString(errMsgData.getScenId(),50));
                ((OraclePreparedStatement)statement).setString(7, nullSafeToString(errMsgData.getUserId(),50));
                ((OraclePreparedStatement)statement).setString(8, nullSafeToString(errMsgData.getStatus(),50));
                ((OraclePreparedStatement)statement).setStringForClob(9, errMsgData.getInputData());
                ((OraclePreparedStatement)statement).setStringForClob(10, errMsgData.getOutputData());
                ((OraclePreparedStatement)statement).setString(11, nullSafeToString(errMsgData.getErrorDesc(),2000));
                ((OraclePreparedStatement)statement).setStringForClob(12, getCustomStackTrace(errMsgData.getErrorStack()));
                ((OraclePreparedStatement)statement).setString(13, nullSafeToString(errMsgData.getIssueStatus(),20));
                ((OraclePreparedStatement)statement).setString(14, nullSafeToString(errMsgData.getIsMailReq(),1));
                ((OraclePreparedStatement)statement).setString(15, nullSafeToString(errMsgData.getEmailTemplateId(),50));
                ((OraclePreparedStatement)statement).setString(16, nullSafeToString(errMsgData.getWorkedOnUser(),50));
                statement.execute();                 
                statement.close(); 
                if (NOTIFICATION_IND_YES.equalsIgnoreCase(nullSafeToString(errMsgData.getIsMailReq()))&&
                    !EMPTY_STRING.equalsIgnoreCase(nullSafeToString(errMsgData.getMailAddress()))) {
                    emailSubject = NOTIFICATION_SUBJECT+" "+getTodayTime();
                    //emailBody = getEmailContent(errMsgData);
                    emailBody = getEmailContentEx(errMsgData);
                    Mail mailHandler = new Mail();
                    mailHandler.sendMail(emailSubject, emailBody, errMsgData.getMailAddress());
                } 
            }           
        } catch (Exception t) {
            t.printStackTrace();
        } finally {
            releaseDBResource(vPriceConn, statement, null, null);
        }
    }

    public static String getEmailContent(VpriceErrorMsgDTO errMsgData) {
        String GET_EMAIL_TEMPLATES = "SELECT TEMPLATE_ID,TEMPLATE FROM " + VPRICE_APP_SCHEMA + ".VPE_EMAIL_TEMPLATES";
        Connection vPriceConn = null;
        Statement statement = null;  
        ResultSet rs = null;
        String templateId = null;
        String template = null;
        String dateTime = null;
        
        try {
            if (emailTemplates==null) {
                synchronized(accesslock) {
                    vPriceConn = getVpriceDBConnection();
                    statement = vPriceConn.createStatement();
                    rs = statement.executeQuery(GET_EMAIL_TEMPLATES); 
                    if (rs!=null) {
                        emailTemplates = new HashMap<String,String>(); 
                        while (rs.next()) {
                            templateId = rs.getString("TEMPLATE_ID");
                            template = rs.getString("TEMPLATE");
                            if (!EMPTY_STRING.equalsIgnoreCase(nullSafeToString(templateId))&&
                                !EMPTY_STRING.equalsIgnoreCase(nullSafeToString(template))) {
                                emailTemplates.put(templateId, template);
                            }
                        }                    
                    }
                }
            }
            if (emailTemplates!=null) {
                template = emailTemplates.get(errMsgData.getEmailTemplateId());
                dateTime = getTodayTime();
                template = template.replaceAll(NOTIFICATION_ELM_DATETIME, dateTime);
                template = template.replaceFirst(NOTIFICATION_ELM_APPNAME, VPRICE_APP);
                template = template.replaceFirst(NOTIFICATION_ELM_ENVADDR, envName);
                template = template.replaceFirst(NOTIFICATION_ELM_ORIGSYS, errMsgData.getOrigSystem());
                template = template.replaceFirst(NOTIFICATION_ELM_SUBSYS, errMsgData.getSubSystem());
                template = template.replaceFirst(NOTIFICATION_ELM_QUOTE, errMsgData.getQuoteId());
                template = template.replaceFirst(NOTIFICATION_ELM_SCEN, errMsgData.getScenId());
                template = template.replaceFirst(NOTIFICATION_ELM_USER, errMsgData.getUserId());
                template = template.replaceFirst(NOTIFICATION_ELM_STATUS, errMsgData.getStatus());
                template = template.replaceFirst(NOTIFICATION_ELM_DESC, errMsgData.getDescription());
                template = template.replaceFirst(NOTIFICATION_ELM_INPUT, nullSafeToString(errMsgData.getInputData()));
                template = template.replaceFirst(NOTIFICATION_ELM_ERR, errMsgData.getErrorDesc()); 
                template = template.replaceFirst(NOTIFICATION_ELM_ERRSTACK, nullSafeToString(getCustomStackTrace(errMsgData.getErrorStack())));            
            }
        } catch (Exception t) {
            t.printStackTrace();
        } finally {
            releaseDBResource(vPriceConn, null, statement, rs);
        }
        return template;
    }
    
    public static String getEmailContentEx(VpriceErrorMsgDTO errMsgData) throws Exception {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_START);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Date Time");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(getTodayTime());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Application");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(VPRICE_APP);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Env");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(envName);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Original System");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getOrigSystem());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Sub-System");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getSubSystem());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Quote ID");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getQuoteId());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Scenario ID");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getScenId());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("User ID");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getUserId());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_START);
        emailContent.append("Status");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_1_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_START);
        emailContent.append(errMsgData.getStatus());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_1_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_START);
        emailContent.append("Description");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_START);
        emailContent.append(errMsgData.getDescription());
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_START);
        emailContent.append("Original Input Data");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_START);
        emailContent.append(EMPTY_STRING);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_START);
        emailContent.append("Output Data");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_START);
        emailContent.append(EMPTY_STRING);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_START);
        emailContent.append("Error Description");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_START);
        emailContent.append(nullSafeToString(errMsgData.getErrorDesc()));
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_END); 
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_START);
        emailContent.append("Error Stack");
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_NAME_PAIR_2_END);
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_START);
        emailContent.append(nullSafeToString(errMsgData.getErrorStack()));
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_VALUE_PAIR_2_END);         
        emailContent.append(MailTemplate.EMAIL_TEMPLATE_END);
        return emailContent.toString();
    }
    
    public static void reportException(ConnectionFactory connFactory, Queue errQueue, VpriceErrorMsgDTO errMsgData) throws Exception {
        javax.jms.Connection conn = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            conn = connFactory.createConnection();
            session = conn.createSession(true, Session.SESSION_TRANSACTED);
            producer = session.createProducer(errQueue);
            ObjectMessage message = session.createObjectMessage();
            message.setObject(errMsgData);
            producer.send(message);
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            releaseResource(conn, session, producer);
        }        
    }
    
    public static String convertObjecttoXmlString(Object inputDTO) {
        String result = EMPTY_STRING;
        try {
            if (inputDTO!=null) {
                XStream xs = new XStream(new DomDriver());
                result = xs.toXML(inputDTO);                
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return result;
    }

    public static String getTodayTime() throws Exception {
        Date now = new Date();
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(now);   
    }
    
    public static String getServerIp() throws Exception {
        //String resp = EMPTY_STRING;
        //InetAddress localHost = InetAddress.getLocalHost();
        //resp = envName;
        return envName;
    }
    
    public static String getCustomStackTrace(Throwable aThrowable) {
        final StringBuilder result = new StringBuilder( "Caught exception @Host-IP:" );
        final String NEW_LINE = System.getProperty("line.separator");
        try {
            if (aThrowable!=null) {              
                result.append(envName);
                result.append(NEW_LINE);
                result.append(aThrowable.toString());
                result.append(NEW_LINE);

                for (StackTraceElement element : aThrowable.getStackTrace() ){
                    result.append( element );
                    result.append( NEW_LINE );
                }                
            } else {
                return null;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return result.toString();
    }
    
    public static void releaseResource(javax.jms.Connection conn, Session session, MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
    }

    public static void releaseDBResource(Connection conn, PreparedStatement statement, Statement statement2, ResultSet resultset) {
        try {
            if (resultset != null) {
                resultset.close();
                resultset = null;
            }
        } catch (Exception t) {
            t.printStackTrace();
        }        
        try {
            if (statement != null) {
                statement.close();
                statement = null;
            }
        } catch (Exception t) {
            t.printStackTrace();
        }
        try {
            if (statement2 != null) {
                statement2.close();
                statement2 = null;
            }
        } catch (Exception t) {
            t.printStackTrace();
        }        
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (Exception t) {
            t.printStackTrace();
        }  
    }
    
    public static String nullSafeToString(Object obj) {
        if (obj == null) {
          return EMPTY_STRING;
        }
        if (obj instanceof String) {
          return ((String)obj).trim();
        }
        return EMPTY_STRING;
    }
    
    public static String nullSafeToString(Object obj, int maxLength) {
        int dataLength = -1;
        if (obj == null) {
          return EMPTY_STRING;
        }
        if (obj instanceof String) {
            dataLength = ((String)obj).trim().length();
            if (dataLength<maxLength) {
                return (((String)obj).trim()).substring(0, dataLength);
            } else {
                return (((String)obj).trim()).substring(0, maxLength);
            }
        }
        return EMPTY_STRING;
    }
    
    public static String getMailServerUrl() {
        String GET_EMAIL_IND = "SELECT PARAM_VALUE FROM "+VPRICE_APP_SCHEMA+".VPRICE_PARAM WHERE PARAM_NAME = 'SOA_URL'";
        Connection vPriceConn = null;
        Statement statement = null;  
        ResultSet rs = null;
        String paramVal = null;
        
        try {
            vPriceConn = getVpriceDBConnection();
            statement = vPriceConn.createStatement();
            rs = statement.executeQuery(GET_EMAIL_IND); 
            if (rs!=null) {
                while (rs.next()) {
                    paramVal = rs.getString("PARAM_VALUE");
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        } finally {
            releaseDBResource(vPriceConn, null, statement, rs);
        }
        return paramVal;
    }    
    
    public static void close(ByteArrayOutputStream baos, ObjectOutputStream oos, ByteArrayInputStream bis,
    		ValidatingObjectInputStream ois) {

		if (baos != null) {
			try {
				baos.close();
			} catch (IOException ignore) {
			}
		}
		if (oos != null) {
			try {
				oos.close();
			} catch (IOException ignore) {
			}
		}
		if (bis != null) {
			try {
				bis.close();
			} catch (IOException ignore) {
			}
		}
		if (ois != null) {
			try {
				ois.close();
			} catch (IOException ignore) {
			}
		}

	}
    
}
