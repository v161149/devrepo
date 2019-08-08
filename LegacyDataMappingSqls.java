package com.verizon.vprice.inventorydataload.impl.util;

import com.verizon.vprice.inventorydataload.impl.scheduler.GetInventoryDataTask;

public class LegacyDataMappingSqls {
    public static final String UPDATE_LEGACY_DATA_PRODS =
        "MERGE INTO "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_DATA_PRODS ca1\n" + 
        "USING "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_DATA_PRODS ca2\n" + 
        "ON (\n" + 
        "  ca1.prod_yr_mth = ca2.prod_yr_mth\n" + 
        "  AND ca1.l4_prod_name = ca2.l4_prod_name\n" + 
        "  AND ca1.cir_chrg_cat = ca2.cir_chrg_cat\n" + 
        "  AND ca1.orig_chrg_code = ca2.orig_chrg_code\n" + 
        "  AND ca1.cir_id = ca2.cir_id\n" + 
        "  AND (ca2.chrg_basis_code like '%D%' or ca2.chrg_basis_code like '%P%')\n" + 
        ")\n" + 
        "WHEN MATCHED THEN UPDATE SET dcnt_amt = ca2.chrg_amt";
    
    public static final String UPDATE_PIP_NODE_CITY = 
        "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_SPEC_DET_STG \n" + 
        "SET RATE_DETER_VALUE = RTRIM(SUBSTR(RATE_DETER_VALUE,1,INSTR(RATE_DETER_VALUE,'-')-1)) \n" + 
        "WHERE RATE_DETER_ID = 'SP_PIP_NODE_CITY' AND RATE_DETER_VALUE LIKE '%-%'";

    public static final String UPDATE_ACCT_NASP_GATEWAY =
        "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_NASP_GATEWAY_STG A \n" + 
        "SET A.NASP_ID = (SELECT DISTINCT B.SUB_NASP_ID\n" + 
        "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACNTHRCHY_REF B\n" + 
        "WHERE A.ACCT_ID = B.ACCOUNT_ID AND ROWNUM=1)";
    
    public static final String UPDATE_LEGACY_ACCT_WITH_GWINFO =
        "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_LINE_ITEM_STG A \n" + 
        "SET (A.GATEWAY_ID, A.GATEWAY_NAME) = (SELECT \n" + 
        "DISTINCT B.GATEWAY_ID, b.GW_NAME FROM VPRICE_LEGACY.LEGACY_ACCT_NASP_GATEWAY_STG B\n" + 
        "WHERE A.ACCT_ID = B.ACCT_ID AND A.NASP_ID = B.NASP_ID AND ROWNUM=1)";
    
    public static final String UPDATE_LEGACY_ACCT_WITH_GWID =
        "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_LINE_ITEM_STG A \n" + 
        "SET A.GATEWAY_ID = (SELECT DISTINCT B.GATEWAY_ID\n" + 
        "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_NASP_GATEWAY_STG B\n" + 
        "WHERE A.ACCT_ID = B.ACCT_ID AND A.NASP_ID = B.NASP_ID AND ROWNUM=1)";
    
    public static final String UPDATE_LEGACY_ACCT_WITH_GWNAME =
        "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_LINE_ITEM_STG A \n" + 
        "SET A.GATEWAY_NAME = (SELECT DISTINCT B.GW_NAME\n" + 
        "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_NASP_GATEWAY_STG B\n" + 
        "WHERE A.ACCT_ID = B.ACCT_ID AND A.NASP_ID = B.NASP_ID AND ROWNUM=1)"; 

    public static final String POST_PROCESSING_CLEANUP_SITE_1 = "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_SITE SET terr_code = 'CO',cntry_name = 'USA' WHERE  cntry_name = 'NA' AND  terr_code ='NA'";
    public static final String POST_PROCESSING_CLEANUP_SITE_2 = "UPDATE "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_SITE SET terr_code = 'CO' WHERE terr_code ='NA' AND cntry_name != 'NA'";
    public static final String POST_PROCESSING_CLEANUP_SPEC_1 = "DELETE FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_SPEC_DET WHERE rate_deter_value = 'NA'";
    public static final String POST_PROCESSING_SELECT_PROD_MONTH = "SELECT DISTINCT A.PROD_YY_MTH, B.IS_METERED FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_PRODUCTS A, "+GetInventoryDataTask.VPRICE_APP_SCHEMA+
                                                                   ".LEGACY_LINE_ITEM B WHERE A.PROD_YY_MTH = B.PROD_YY_MTH AND A.PROD_CODE = B.PROD";
    public static final String POST_PROCESSING_INSERT_PROD_MONTH = "INSERT INTO "+GetInventoryDataTask.VPRICE_APP_SCHEMA +".LEGACY_PROD_MONTH(IS_METERED,PROD_YY_MTH) VALUES(?,?)";
    public static final String POST_PROCESSING_SP_UPDATE_ACCESS_TYPE = "{CALL legacyupdates.updatelegacyprodaccesstype(?)}"; //legacyupdates.updatelegacyprodaccesstype(yearmonth in varchar2)
   
    public static final String POST_PROCESSING_LOAD_LEGACY_ACCT_NASP_GW ="MERGE INTO "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_NASP_GATEWAY A  \n" + 
                                                                         "USING "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".LEGACY_ACCT_NASP_GATEWAY_STG B ON (A.ACCT_ID = B.ACCT_ID AND A.NASP_ID = B.NASP_ID AND A.GATEWAY_ID = B.GATEWAY_ID)  \n" + 
                                                                         "WHEN NOT MATCHED THEN INSERT (A.LEGACY_ACCT_NASP_GATEWAY_ID,A.ACCT_ID,A.NASP_ID,A.GATEWAY_ID,A.GW_NAME)  \n" + 
                                                                         "VALUES (SEQ_LEGACY_ACCT_NASP_GATEWAY.NEXTVAL,B.ACCT_ID,B.NASP_ID,B.GATEWAY_ID,B.GW_NAME)";
    
    public static final String GET_VRD_DATA = 
                        "SELECT DISTINCT \n" + 
                        "       PROD_YR_MTH, \n" + 
                        "       CIR_ID, \n" + 
                        "       MAN, \n" + 
                        "       NASP_ID, \n" + 
                        "       SUB_NASP_ID, \n" + 
                        "       BAN, \n" + 
                        "       SITE_ADDR1, \n" + 
                        "       SITE_ADDR2, \n" + 
                        "       CITY, \n" + 
                        "       ST, \n" + 
                        "       POST_CDE, \n" + 
                        "       CNTRY_CDE, \n" + 
                        "       PRODUCT_ID, \n" + 
                        "       PRODUCT_FEATURE_ID, \n" + 
                        "       INV_LTRL, \n" + 
                        "       RPTG_CRCTP, \n" + 
                        "       ACTUAL_PORT_SPEED AS DW_CIR_SPEED, \n" + //Srini suggested use PIP_PORT_SPEED replace RPTG_CIR_SPEED
                        "       RPTG_CIR_SPEED, \n" + 
                        "       TERM_CLLI, \n" + 
                        "       ORIG_CLLI, \n" +
                        "       RATE_REC, \n" +
                        "       DISCOUNT_REC, \n" +
                        "       PROMO_REC, \n" +
                        "       TERM, \n" +
                        "       SPECIFICATION_ID_1, \n" +
                        "       SPECIFICATION_VALUE_NAME_1, \n" +
                        "       SPECIFICATION_ID_2, \n" +
                        "       SPECIFICATION_VALUE_NAME_2, \n" +
                        "       SPECIFICATION_ID_3, \n" +
                        "       SPECIFICATION_VALUE_NAME_3, \n" +
                        "       SPECIFICATION_ID_4, \n" +
                        "       SPECIFICATION_VALUE_NAME_4, \n" +
                        "       SPECIFICATION_ID_5, \n" +
                        "       SPECIFICATION_VALUE_NAME_5, \n" +
                        "       SPECIFICATION_ID_6, \n" +
                        "       SPECIFICATION_VALUE_NAME_6, \n" +
                        "       SPECIFICATION_ID_7, \n" +
                        "       SPECIFICATION_VALUE_NAME_7, \n" +
                        "       SPECIFICATION_ID_8, \n" +
                        "       SPECIFICATION_VALUE_NAME_8, \n" +
                        "       SPECIFICATION_ID_9, \n" +
                        "       SPECIFICATION_VALUE_NAME_9, \n" +
                        "       SPECIFICATION_ID_10, \n" +
                        "       SPECIFICATION_VALUE_NAME_10, \n" +
                        "       SPECIFICATION_ID_11, \n" +
                        "       SPECIFICATION_VALUE_NAME_11, \n" +
                        "       SPECIFICATION_ID_12, \n" +
                        "       SPECIFICATION_VALUE_NAME_12, \n" +
                        "       SPECIFICATION_ID_13, \n" +
                        "       SPECIFICATION_VALUE_NAME_13, \n" +
                        "       SPECIFICATION_ID_14, \n" +
                        "       SPECIFICATION_VALUE_NAME_14, \n" +
                        "       SPECIFICATION_ID_15, \n" +
                        "       SPECIFICATION_VALUE_NAME_15, \n" +
                        "       SPECIFICATION_ID_16, \n" +
                        "       SPECIFICATION_VALUE_NAME_16, \n" +
                        "       SPECIFICATION_ID_17, \n" +
                        "       SPECIFICATION_VALUE_NAME_17, \n" +
                        "       SPECIFICATION_ID_18, \n" +
                        "       SPECIFICATION_VALUE_NAME_18, \n" +
                        "       SPECIFICATION_ID_19, \n" +
                        "       SPECIFICATION_VALUE_NAME_19, \n" +
                        "       SPECIFICATION_ID_20, \n" +
                        "       SPECIFICATION_VALUE_NAME_20 \n" +                        
                        "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".PBLI_REPORT \n" +
                        "WHERE PROD_YR_MTH = ? AND PRODUCT_ID = ? AND (ACTIVE_CONFIG IS NULL OR ACTIVE_CONFIG = 'Y') ORDER BY MAN";
    
    public static final String GET_NONMETERED_DATA = 
        		"SELECT DISTINCT \n" + 
        		"       A.PROD_YR_MTH AS PROD_YY_MTH, \n" + 
        		//"       A.ID, \n" + 
        		"       A.CIR_ID, \n" + 
        		//"       A.ITEM_ID, \n" + 
        		"       A.MAN AS ACCT_ID, \n" + 
        		"       A.NASP_ID, \n" + 
        		"       A.SUB_NASP_ID, \n" + 
        		"       A.BAN, \n" + 
        		"       A.SVN_ADDR_LINE_1 AS SITE_ADDR1, \n" + 
        		"       A.SVN_ADDR_LINE_2 AS SITE_ADDR2, \n" + 
        		"       A.SVN_ADDR_LINE_3 AS SITE_ADDR3, \n" + 
                        "       A.OB_SVC_LOCATION, \n" +
        		"       INITCAP(A.SVN_CITY_NAME) AS CITY, \n" + 
                        "       INITCAP(B.PIP_MODEL_LOC_CONC) AS CITY2, \n" +
                        "       B.NODE_NAME, \n" +
                        "       B.NODE_ID, \n" +
                        "       B.SWITCH_OWNER, \n" +
                        "       B.CARRIER_NAME2, \n" +
                        "       B.PIP_MODEL_LOC_CONC, \n" +
                        "       B.PIP_MODEL_COUNTRY, \n" +
                        "       A.SVN_TERR_CODE, \n" + 
        		"       A.SVN_TERR_CODE_DESC_TEXT AS TERR_CODE_DESC, \n" + 
        		"       A.SVN_POSTAL_NUM, \n" + 
        		"       A.SVN_CNTRY_NAME AS CNTRY_NAME, \n" + 
 			    "       A.L3_PROD_NAME AS PROD_NME3, \n" +
        		"       A.L4_PROD_NAME AS PROD_NME, \n" + 
        		"       A.CIR_CHRG_CAT AS PROD_FEAT_NME, \n" + 
                        //"       A.JURIS_CODE, \n" + //GULSHAN.Production_Support - Defect # 1932
                        "       A.L6_PROD_NAME, \n" +
        		"       A.CHRG_BASIS_CODE, \n" + 
                        "       A.MLG_QTY, \n" +
        		"       A.INV_LTRL, \n" + 
        		"       A.CHRG_CODE_DESC_TEXT AS DW_CHRG_CODE_DESC, \n" + 
        		"       A.RPTG_CRCTP AS DW_RPTG_CRCTP, \n" + 
                        "       A.PIP_PORT_SPEED AS DW_CIR_SPEED, \n" + //Srini suggested use PIP_PORT_SPEED replace RPTG_CIR_SPEED
        		"       A.RPTG_CIR_SPEED, \n" + 
        		"       A.ORGNL_CIR_SPEED AS DW_ORGNL_CIR_SPEED, \n" + 
        		"       A.ORIG_ISO_CNTRY_ID AS DW_ISO_CNTRY_ID, \n" + 
        		"       A.TERM_CLLI, \n" + 
        		"       A.ORIG_CLLI AS CLLI, \n" +
                        "       A.FEED, \n" +
                        "       A.CIR_CMPNT_ID, \n" +
                        //"       A.ACCS_CIR_ID, \n" + //removed it because NxT1 issue
                        "       A.RPTG_CNTRY_NAME, \n" +
                        "       A.CHRG_AMT, \n" +
                        "       A.DCNT_AMT, \n" +
                        "       A.ORIG_CHRG_CODE, \n" +
                        "       A.CHRG_FROM_DATE, \n" +
                        "       A.CHRG_TO_DATE, \n" +
                        "       count(*) AS LINE_ITEM_QTY \n" +
        		"from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_data_prods a\n" + 
        		"left outer join (select * from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_nopatdata_12m bb where bb.year_month >= \n" + 
        		"(select max(bbb.year_month) from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_nopatdata_12m bbb where bbb.circuit_id = bb.circuit_id)) b\n" + 
        		"  on b.circuit_id = a.cir_id\n" + 
        		"where a.prod_yr_mth = ? and \n" + 
        		"      a.l4_prod_name = ? and \n" + 
        		"      a.year = ? and \n" + 
        		"      a.month = ? and\n" + 
        		"      a.chrg_basis_code like '%R%' and \n" + 
        		"      a.feed not in ('ZPRB') \n" +
                        "group by\n" + 
                        "       A.PROD_YR_MTH, \n" + 
                        "       A.CIR_ID, \n" + 
                        "       A.MAN, \n" + 
                        "       A.NASP_ID, \n" + 
                        "       A.SUB_NASP_ID, \n" + 
                        "       A.BAN, \n" + 
                        "       A.SVN_ADDR_LINE_1, \n" + 
                        "       A.SVN_ADDR_LINE_2, \n" + 
                        "       A.SVN_ADDR_LINE_3, \n" + 
                        "       A.OB_SVC_LOCATION, \n" + 
                        "       INITCAP(A.SVN_CITY_NAME), \n" + 
                        "       INITCAP(B.PIP_MODEL_LOC_CONC), \n" + 
                        "       B.NODE_NAME, \n" + 
                        "       B.NODE_ID, \n" + 
                        "       B.SWITCH_OWNER, \n" + 
                        "       B.CARRIER_NAME2, \n" + 
                        "       B.PIP_MODEL_LOC_CONC, \n" + 
                        "       B.PIP_MODEL_COUNTRY, \n" + 
                        "       A.SVN_TERR_CODE, \n" + 
                        "       A.SVN_TERR_CODE_DESC_TEXT, \n" + 
                        "       A.SVN_POSTAL_NUM, \n" + 
                        "       A.SVN_CNTRY_NAME, \n" +
                        "       A.L3_PROD_NAME, \n" +
                        "       A.L4_PROD_NAME, \n" + 
                        "       A.CIR_CHRG_CAT, \n" + 
                        //"       A.JURIS_CODE, \n" + 
                        "       A.L6_PROD_NAME, \n" + 
                        "       A.CHRG_BASIS_CODE, \n" + 
                        "       A.MLG_QTY, \n" + 
                        "       A.INV_LTRL, \n" + 
                        "       A.CHRG_CODE_DESC_TEXT, \n" + 
                        "       A.RPTG_CRCTP, \n" + 
                        "       A.PIP_PORT_SPEED, \n" + 
                        "       A.RPTG_CIR_SPEED, \n" + 
                        "       A.ORGNL_CIR_SPEED, \n" + 
                        "       A.ORIG_ISO_CNTRY_ID, \n" + 
                        "       A.TERM_CLLI, \n" + 
                        "       A.ORIG_CLLI, \n" + 
                        "       A.FEED, \n" + 
                        "       A.CIR_CMPNT_ID, \n" + 
                        "       A.RPTG_CNTRY_NAME, \n" + 
                        "       A.CHRG_AMT, \n" + 
                        "       A.DCNT_AMT, \n" + 
                        "       A.ORIG_CHRG_CODE, \n" + 
                        "       A.CHRG_FROM_DATE, \n" + 
                        "       A.CHRG_TO_DATE \n" +
        		"order by a.man";

    //Broadband
    public static final String GET_BROADBAND_DATA = 
                    "SELECT DISTINCT \n" + 
                    "       A.YR_MONT AS PROD_YY_MTH, \n" + 
                    "       A.CIRCUIT AS CIR_ID, \n" + 
                    //"       A.ITEM_ID, \n" + 
                    "       A.MAN AS ACCT_ID, \n" + 
                    "       SUBSTR(A.SUB_NASP_ID,1,6) AS NASP_ID, \n" + 
                    "       A.SUB_NASP_ID, \n" + 
                    "       A.BAN, \n" + 
                    "       A.SVN_ADDR_LINE_1 AS SITE_ADDR1, \n" + 
                    "       A.SVN_ADDR_LINE_2 AS SITE_ADDR2, \n" + 
                    "       A.SVN_ADDR_LINE_3 AS SITE_ADDR3, \n" +
                    //"       A.OB_SVC_LOCATION, \n" +
                    "       A.SVN_CITY_NAME AS CITY, \n" + 
                    "       A.SVN_TERR_CODE, \n" + 
                    "       A.SVN_TERR_CODE_DESC_TEXT AS TERR_CODE_DESC, \n" + 
                    "       A.SVN_POSTAL_NUM, \n" + 
                    "       A.SVN_CNTRY_NAME AS CNTRY_NAME, \n" + 
                    "       A.L4_PROD_NAME AS PROD_NME, \n" + 
                    "       A.DW_CIR_CHRG_CAT AS PROD_FEAT_NME, \n" + 
                    "       A.DW_CHRG_BASIS_CODE AS CHRG_BASIS_CODE, \n" + 
                    "       A.INVOICE_LITERAL_TEXT AS INV_LTRL, \n" + 
                    "       A.DW_CHRG_CODE_DESC AS DW_CHRG_CODE_DESC, \n" + 
                    "       A.DW_RPTG_CRCTP, \n" + 
                    "       A.DW_SPEED AS DW_CIR_SPEED, \n" + 
                    "       A.DW_SPEED AS DW_ORGNL_CIR_SPEED, \n" + 
                    "       A.ORIG_ISO_CNTRY_ID AS DW_ISO_CNTRY_ID, \n" + 
                    "       A.TERM_CLLI, \n" + 
                    "       A.ORIG_CLLI AS CLLI, \n" + 
                    "       A.IC_VENDOR_NAME, \n" + 
                    "       A.ZIP_CODE_CABLE_CARRIER, \n" + 
                    "       A.CHRG_AMT, \n" +
                    "       A.DCNT_AMT, \n" + 
                    "       A.CHRG_FROM_DATE, \n" +
                    "       A.CHRG_TO_DATE \n" +                     
                    "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".V_INTERNET_DSL A \n" +
                    "WHERE A.YR_MONT = ? AND A.DW_CHRG_BASIS_CODE LIKE '%R%' AND A.FEED NOT IN ('ZPRB') ORDER BY A.MAN";

    //IP VPN Broadband and IP VPN Remote Access
    public static final String GET_IPVPN_DATA = 
                    "SELECT DISTINCT \n" + 
                    "       A.YR_MONT AS PROD_YY_MTH, \n" + 
                    "       A.CIRCUIT AS CIR_ID, \n" + 
                    //"       A.ITEM_ID, \n" + 
                    "       A.MAN AS ACCT_ID, \n" + 
                    "       SUBSTR(A.SUB_NASP_ID,1,6) AS NASP_ID, \n" + 
                    "       A.SUB_NASP_ID, \n" + 
                    "       A.BAN, \n" + 
                    "       A.SVN_ADDR_LINE_1 AS SITE_ADDR1, \n" + 
                    "       A.SVN_ADDR_LINE_2 AS SITE_ADDR2, \n" + 
                    "       A.SVN_ADDR_LINE_3 AS SITE_ADDR3, \n" + 
                    //"       A.OB_SVC_LOCATION, \n" +
                    "       A.SVN_CITY_NAME AS CITY, \n" + 
                    "       A.SVN_TERR_CODE, \n" + 
                    "       A.SVN_TERR_CODE_DESC_TEXT AS TERR_CODE_DESC, \n" + 
                    "       A.SVN_POSTAL_NUM, \n" + 
                    "       A.SVN_CNTRY_NAME AS CNTRY_NAME, \n" + 
                    "       A.L4_PROD_NAME AS PROD_NME, \n" + 
                    "       A.DW_CIR_CHRG_CAT AS PROD_FEAT_NME, \n" + 
                    "       A.DW_CHRG_BASIS_CODE AS CHRG_BASIS_CODE, \n" + 
                    "       A.INVOICE_LITERAL_TEXT AS INV_LTRL, \n" + 
                    "       A.DW_CHRG_CODE_DESC AS DW_CHRG_CODE_DESC, \n" + 
                    "       A.DW_RPTG_CRCTP, \n" + 
                    "       A.DW_SPEED AS DW_CIR_SPEED, \n" + 
                    "       A.DW_SPEED AS DW_ORGNL_CIR_SPEED, \n" + 
                    "       A.ORIG_ISO_CNTRY_ID AS DW_ISO_CNTRY_ID, \n" + 
                    "       A.TERM_CLLI, \n" + 
                    "       A.ORIG_CLLI AS CLLI, \n" + 
                    "       A.IC_VENDOR_NAME, \n" + 
                    "       A.ZIP_CODE_CABLE_CARRIER, \n" + 
                    "       A.BCP_DISPLAY_NAME, \n" +
                    "       A.CHRG_AMT, \n" +
                    "       A.DCNT_AMT, \n" + 
                    "       A.CHRG_FROM_DATE, \n" +
                    "       A.CHRG_TO_DATE \n" +                     
                    "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".V_IP_VPN A \n" + 
                    "WHERE A.YR_MONT = ? AND A.DW_CHRG_BASIS_CODE LIKE '%R%' AND A.FEED NOT IN ('ZPRB') ORDER BY A.MAN";

    //Private Line - US - TDM
    public static final String GET_USPL_DATA = 
                        "SELECT \n" + 
                        "       A.YR_MONT AS PROD_YY_MTH, \n" + 
                        "       A.CIRCUIT AS CIR_ID, \n" + 
                        //"       A.ITEM_ID, \n" + 
                        "       A.MAN AS ACCT_ID, \n" + 
                        "       SUBSTR(A.SUB_NASP_ID,1,6) AS NASP_ID, \n" + 
                        "       A.SUB_NASP_ID, \n" + 
                        "       A.BAN, \n" + 
                        "       A.SVN_ADDR_LINE_1 AS SITE_ADDR1, \n" + 
                        "       A.SVN_ADDR_LINE_2 AS SITE_ADDR2, \n" + 
                        "       A.SVN_ADDR_LINE_3 AS SITE_ADDR3, \n" + 
                        //"       A.OB_SVC_LOCATION, \n" +
                        "       A.SVN_CITY_NAME AS CITY, \n" + 
                        "       A.SVN_TERR_CODE, \n" + 
                        "       A.SVN_TERR_CODE_DESC_TEXT AS TERR_CODE_DESC, \n" + 
                        "       A.SVN_POSTAL_NUM, \n" + 
                        "       A.SVN_CNTRY_NAME AS CNTRY_NAME, \n" + 
                        "       A.L4_PROD_NAME AS PROD_NME, \n" + 
                        "       A.DW_CIR_CHRG_CAT AS PROD_FEAT_NME, \n" + 
                        "       A.L6_PROD_NAME, \n" +
                        "       A.DW_CHRG_BASIS_CODE AS CHRG_BASIS_CODE, \n" + 
                        "       A.DW_CHRG_CODE_DESC AS DW_CHRG_CODE_DESC, \n" + 
                        "       A.DW_RPTG_CRCTP, \n" + 
                        "       A.DW_SPEED AS DW_CIR_SPEED, \n" + 
                        "       A.DW_SPEED AS DW_ORGNL_CIR_SPEED, \n" + 
                        "       A.ORIG_ISO_CNTRY_ID AS DW_ISO_CNTRY_ID, \n" + 
                        "       A.TERM_CLLI, \n" + 
                        "       A.ORIG_CLLI AS CLLI, \n" + 
                        "       A.MILEAGE, \n" +
                        "       A.CHRG_AMT, \n" +
                        "       A.DCNT_AMT, \n" + 
                        "       A.CHRG_FROM_DATE, \n" +
                        "       A.CHRG_TO_DATE, \n"  +
                        "       COUNT(*) AS USPL_QTY \n"  +
                        "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".V_DOMESTIC_PRIVATE_LINE A \n" +
                        "WHERE A.YR_MONT = ? AND A.L4_PROD_NAME = ? AND A.DW_CHRG_BASIS_CODE LIKE '%R%' AND A.DW_CIR_CHRG_CAT NOT IN ('ACCESS') AND A.FEED NOT IN ('ZPRB') \n" +
                        "GROUP BY A.YR_MONT,  \n" + 
                        "       A.CIRCUIT,  \n" + 
                        "       A.MAN,  \n" + 
                        "       SUBSTR(A.SUB_NASP_ID,1,6),  \n" + 
                        "       A.SUB_NASP_ID,  \n" + 
                        "       A.BAN,  \n" + 
                        "       A.SVN_ADDR_LINE_1,  \n" + 
                        "       A.SVN_ADDR_LINE_2,  \n" + 
                        "       A.SVN_ADDR_LINE_3,  \n" + 
                        "       A.SVN_CITY_NAME,  \n" + 
                        "       A.SVN_TERR_CODE,  \n" + 
                        "       A.SVN_TERR_CODE_DESC_TEXT,  \n" + 
                        "       A.SVN_POSTAL_NUM,  \n" + 
                        "       A.SVN_CNTRY_NAME,  \n" + 
                        "       A.L4_PROD_NAME,  \n" + 
                        "       A.DW_CIR_CHRG_CAT,  \n" + 
                        "       A.L6_PROD_NAME, \n" + 
                        "       A.DW_CHRG_BASIS_CODE,  \n" + 
                        "       A.DW_CHRG_CODE_DESC,  \n" + 
                        "       A.DW_RPTG_CRCTP,  \n" + 
                        "       A.DW_SPEED,   \n" + 
                        "       A.ORIG_ISO_CNTRY_ID,  \n" + 
                        "       A.TERM_CLLI,  \n" + 
                        "       A.ORIG_CLLI,  \n" + 
                        "       A.MILEAGE, \n" + 
                        "       A.CHRG_AMT, \n" + 
                        "       A.DCNT_AMT,  \n" + 
                        "       A.CHRG_FROM_DATE, \n" + 
                        "       A.CHRG_TO_DATE \n" +
                        "ORDER BY A.MAN";
    
    public static final String GET_LD_VOICE_DATA_IB = 
                    "       SELECT A.YEAR_MONTH, \n" +
                    "              A.IDVAL AS NASP_ID, \n" + 
                    "              A.ACCT_ID,   \n" + 
                    "              A.ORIG_COUNTRY_NAME AS COUNTRY_NAME,\n" + 
                    "              A.ORIG_STATE_NAME AS STATE_NAME,       \n" + 
                    "              A.ONNET_TRANS_CD,\n" + 
                    "              A.ONNET_TRANS_CODE_DESC,\n" + 
                    "              SUM(A.CALL_COUNT) AS USAGE_AMT, \n" + 
                    "              SUM(A.CALL_MIN) AS QTY,  \n" + 
                    "              SUM(A.BILLING_RATE) AS CHRG_AMT  \n" +  
                    //"              SUM(A.DCNT_AMT) AS DCNT_AMT  \n" + 
                    "       FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".prodFeatureTbl A \n" + 
                    "       WHERE A.YEAR_MONTH = ? AND A.IDTYPE = 'NASP' AND \n" +
                    "             A.ONNET_TRANS_CD IN ('Sw/Loc','Sw/Ded','Loc/Sw','Sw/Sw','Ded/Ded','Loc/Ded','Ded/Loc','Ded/Sw','Loc/Loc') \n" +
                    "       GROUP BY A.YEAR_MONTH, \n" +
                    "                A.IDVAL,\n" + 
                    "                A.ACCT_ID,\n" + 
                    "                A.ORIG_COUNTRY_NAME,\n" + 
                    "                A.ORIG_STATE_NAME,  \n" + 
                    "                A.ONNET_TRANS_CD,\n" + 
                    "                A.ONNET_TRANS_CODE_DESC ORDER BY A.ACCT_ID";

    public static final String GET_LD_VOICE_DATA_OB = 
                    "       SELECT A.YEAR_MONTH, \n" +
                    "              A.IDVAL AS NASP_ID,\n" + 
                    "              A.ACCT_ID,   \n" + 
                    "              A.TERM_COUNTRY_NAME AS COUNTRY_NAME,\n" + 
                    "              A.TERM_STATE_NAME AS STATE_NAME,       \n" + 
                    "              A.ONNET_TRANS_CD,\n" + 
                    "              A.ONNET_TRANS_CODE_DESC,\n" + 
                    "              SUM(A.CALL_COUNT) AS USAGE_AMT, \n" + 
                    "              SUM(A.CALL_MIN) AS QTY,  \n" +
                    "              SUM(A.BILLING_RATE) AS CHRG_AMT  \n" +  
                    //"              SUM(A.DCNT_AMT) AS DCNT_AMT  \n" +                    
                    "       FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".prodFeatureTbl A \n" + 
                    "       WHERE A.YEAR_MONTH = ? AND A.IDTYPE = 'NASP' AND \n" + 
                    "             A.ONNET_TRANS_CD IN ('Sw/Loc','Sw/Ded','Loc/Sw','Sw/Sw','Ded/Ded','Loc/Ded','Ded/Loc','Ded/Sw','Loc/Loc') \n" +
                    "       GROUP BY A.YEAR_MONTH,\n" +
                    "                A.IDVAL,\n" + 
                    "                A.ACCT_ID,\n" + 
                    "                A.TERM_COUNTRY_NAME,\n" + 
                    "                A.TERM_STATE_NAME,  \n" + 
                    "                A.ONNET_TRANS_CD,\n" + 
                    "                A.ONNET_TRANS_CODE_DESC ORDER BY A.ACCT_ID"; 
    
    //Audio Conferencing, Video Conferencing
    public static final String GET_CONF_DATA = 
                    "SELECT " +
                    "       A.MONT_BILL_GK AS YEAR_MONTH,\n" + 
                    "       A.IDVAL AS NASP_ID,\n" + 
                    "       A.ACCT_ID_NK AS ACCT_ID,\n" + 
                    "       A.FEED,\n" +
                    "	    A.BAN,\n" + 
                    "       A.BCP_PRODUCT_NAME,\n" +
                    "       A.SUB_GROUP,\n" + 
                    "       A.RATE_ELEMENT,\n" +  
                    "	    A.BRDG_CTR_NAME,\n" + 
                    "       A.COUNTRY AS COUNTRY_NAME,\n" +
                    "       SUM(A.USAGE) AS USAGE_AMT,\n" + 
                    "       SUM(A.BILLING_RATE) AS CHRG_AMT  \n" +  
                    //"       SUM(A.DCNT_AMT) AS DCNT_AMT  \n" +                    
                    "FROM "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".BCP_VW_CONF_INVENTORY_SUMMARY A\n" + 
                    "WHERE A.MONT_BILL_GK = ? AND\n" +
                    "      A.IDTYPE = 'NASP' AND\n" + 
                    "      A.FEED NOT IN ('ZPRB') AND\n" +
                    //"      A.BCP_PRODUCT_NAME IN ('Audio Conferencing', 'Video Conferencing')\n"+
                    "      A.BCP_PRODUCT_NAME IN ('Audio Conferencing')\n"+
                    "GROUP BY " +
                    "      A.MONT_BILL_GK,\n"+
                    "      A.IDVAL,\n"+
                    "      A.ACCT_ID_NK,\n"+
                    "      A.FEED,\n"+
                    "      A.BAN,\n"+
                    "      A.BCP_PRODUCT_NAME,\n"+
                    "      A.SUB_GROUP,\n"+
                    "      A.RATE_ELEMENT,\n"+
                    "      A.BRDG_CTR_NAME,\n"+
                    "      A.COUNTRY ORDER BY A.ACCT_ID_NK";
    
    //Single row ACC
    public static final String GET_SINGLE_ROW_ACC_DATA = "select distinct a.li_id,a.qty, a.legacy_site_id,a.cw_feat_item_id, r.amount, d.rate_deter_id, d.rate_deter_value, \n" + 
                    "       b.cameo_carrier_abbr, b.rptg_cea_type, b.legacy_cea_type, b.tpv_uni_speed,b.circuit_type, b.access_revenue, b.ethernet_cos,b.mapped_legacy_ethernet_cos,b.mapped_vrd_ethernet_cos,b.evc_los," +
                    "       b.onnet_prov,b.first_wc,b.first_street, b.first_city, b.first_state, b.first_zip, b.first_cntry_code,a.is_vrd,\n" +
                    "       vaa.cir_id, \n" + 
                    "       vaa.SPECIFICATION_ID_1,vaa.SPECIFICATION_VALUE_NAME_1, \n" + 
                    "       vaa.SPECIFICATION_ID_2, vaa.SPECIFICATION_VALUE_NAME_2, \n" + 
                    "       vaa.SPECIFICATION_ID_3, vaa.SPECIFICATION_VALUE_NAME_3, \n" + 
                    "       vaa.SPECIFICATION_ID_4, vaa.SPECIFICATION_VALUE_NAME_4, \n" + 
                    "       vaa.SPECIFICATION_ID_5, vaa.SPECIFICATION_VALUE_NAME_5, \n" + 
                    "       vaa.SPECIFICATION_ID_6, vaa.SPECIFICATION_VALUE_NAME_6, \n" + 
                    "       vaa.SPECIFICATION_ID_7, vaa.SPECIFICATION_VALUE_NAME_7, \n" + 
                    "       vaa.SPECIFICATION_ID_8, vaa.SPECIFICATION_VALUE_NAME_8, \n" + 
                    "       vaa.SPECIFICATION_ID_9, vaa.SPECIFICATION_VALUE_NAME_9, \n" + 
                    "       vaa.SPECIFICATION_ID_10, vaa.SPECIFICATION_VALUE_NAME_10, \n" + 
                    "       vaa.SPECIFICATION_ID_11, vaa.SPECIFICATION_VALUE_NAME_11, \n" + 
                    "       vaa.SPECIFICATION_ID_12, vaa.SPECIFICATION_VALUE_NAME_12, \n" + 
                    "       vaa.SPECIFICATION_ID_13, vaa.SPECIFICATION_VALUE_NAME_13, \n" + 
                    "       vaa.SPECIFICATION_ID_14, vaa.SPECIFICATION_VALUE_NAME_14, \n" + 
                    "       vaa.SPECIFICATION_ID_15, vaa.SPECIFICATION_VALUE_NAME_15, \n" + 
                    "       vaa.SPECIFICATION_ID_16, vaa.SPECIFICATION_VALUE_NAME_16, \n" + 
                    "       vaa.SPECIFICATION_ID_17, vaa.SPECIFICATION_VALUE_NAME_17, \n" + 
                    "       vaa.SPECIFICATION_ID_18, vaa.SPECIFICATION_VALUE_NAME_18, \n" + 
                    "       vaa.SPECIFICATION_ID_19, vaa.SPECIFICATION_VALUE_NAME_19, \n" + 
                    "       vaa.SPECIFICATION_ID_20, vaa.SPECIFICATION_VALUE_NAME_20 \n"+                                     
                    "from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_line_item_single_row a\n" + 
                    "left outer join "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_rate_single_row r\n" + 
                    "on r.leg_li_id = a.li_id\n" + 
                    "inner join "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".legacy_spec_det_single_row d\n" + 
                    "on d.leg_li_id = a.li_id\n" + 
                    "left outer join "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".bi_type_feed3 b \n" + 
                    "on b.cir_id = a.cw_feat_item_id and b.prod_yr_mth=?\n" + 
                    "left outer join ( \n" + 
                    "    select vb.cir_id, \n" + 
                    "       vb.SPECIFICATION_ID_1,vb.SPECIFICATION_VALUE_NAME_1, \n" + 
                    "       vb.SPECIFICATION_ID_2, vb.SPECIFICATION_VALUE_NAME_2, \n" + 
                    "       vb.SPECIFICATION_ID_3, vb.SPECIFICATION_VALUE_NAME_3, \n" + 
                    "       vb.SPECIFICATION_ID_4, vb.SPECIFICATION_VALUE_NAME_4, \n" + 
                    "       vb.SPECIFICATION_ID_5, vb.SPECIFICATION_VALUE_NAME_5, \n" + 
                    "       vb.SPECIFICATION_ID_6, vb.SPECIFICATION_VALUE_NAME_6, \n" + 
                    "       vb.SPECIFICATION_ID_7, vb.SPECIFICATION_VALUE_NAME_7, \n" + 
                    "       vb.SPECIFICATION_ID_8, vb.SPECIFICATION_VALUE_NAME_8, \n" + 
                    "       vb.SPECIFICATION_ID_9, vb.SPECIFICATION_VALUE_NAME_9, \n" + 
                    "       vb.SPECIFICATION_ID_10, vb.SPECIFICATION_VALUE_NAME_10, \n" + 
                    "       vb.SPECIFICATION_ID_11, vb.SPECIFICATION_VALUE_NAME_11, \n" + 
                    "       vb.SPECIFICATION_ID_12, vb.SPECIFICATION_VALUE_NAME_12, \n" + 
                    "       vb.SPECIFICATION_ID_13, vb.SPECIFICATION_VALUE_NAME_13, \n" + 
                    "       vb.SPECIFICATION_ID_14, vb.SPECIFICATION_VALUE_NAME_14, \n" + 
                    "       vb.SPECIFICATION_ID_15, vb.SPECIFICATION_VALUE_NAME_15, \n" + 
                    "       vb.SPECIFICATION_ID_16, vb.SPECIFICATION_VALUE_NAME_16, \n" + 
                    "       vb.SPECIFICATION_ID_17, vb.SPECIFICATION_VALUE_NAME_17, \n" + 
                    "       vb.SPECIFICATION_ID_18, vb.SPECIFICATION_VALUE_NAME_18, \n" + 
                    "       vb.SPECIFICATION_ID_19, vb.SPECIFICATION_VALUE_NAME_19, \n" + 
                    "       vb.SPECIFICATION_ID_20, vb.SPECIFICATION_VALUE_NAME_20 \n" + 
                    "    from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".PBLI_REPORT vb\n" + 
                    "    where vb.product_id ='PR_ACC' and (vb.ACTIVE_CONFIG IS NULL OR vb.ACTIVE_CONFIG = 'Y')\n" + 
                    ") vaa on vaa.cir_id=a.cw_feat_item_id " +                                     
                    "where a.prod_yy_mth = ? and a.prod='PR_ACC' and a.is_metered=0";
    //Single row ACC Speed
    public static final String GET_SINGLE_ROW_ACC_SPEED ="select access_speed_kbps from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".bi_type_feed3 where prod_yr_mth=? and cir_id=? and ROWNUM=1";
    //Single row ACC CLLI
    public static final String GET_SINGLE_ROW_ACC_CLLI ="select FIRST_WC from "+GetInventoryDataTask.VPRICE_APP_SCHEMA+".bi_type_feed3 where prod_yr_mth=? and cir_id=? and ROWNUM=1";
}
