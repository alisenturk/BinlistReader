/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.binlist.binlistreader;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


/**
 *
 * @author asenturk
 */
public class BinlistReader {
    private static String API_URL = "https://binlist.p.mashape.com/json/";
    public static String[] postRequestV3(String binnumber){
        String[] result = new String[3];
        try {
            HttpResponse<JsonNode> response = Unirest.get(API_URL +binnumber)
                                                     .header("X-Mashape-Key", "KwjwH0AOSlmshIQ6iQtvlcsAFIPcp1lzdVijsn3eLnVDCV229k")
                                                     .header("Accept", "application/json").asJson();
            
            result[0] = response.getBody().getObject().get("country_code").toString();
            result[1] = response.getBody().getObject().get("country_name").toString();
            result[2] = response.getBody().getObject().get("bank").toString();

        } catch (UnirestException ex) {
            Logger.getLogger(BinlistReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    public static String checkNull(String obj,String newstr){
        if(obj==null)return newstr;
        else return obj;
    }
    public static void main(String[] args){
        String result[] = null;
        
        String folder = "/opt/";
        String sourceFileName = "binlist.xls";
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(folder+sourceFileName);
            
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Iterator<Cell> cellIterator = null;
            Row     row     = null;
            Cell    cell    = null;
            int cellNo=0;
            String  binno           = "";
            String  longUrl         = "";
            String  shortUrl        = "";
            int rownum = 0;
            while(rowIterator.hasNext()){
                rownum++;
                if(rownum==1)continue;
                
                row = rowIterator.next();
                cellIterator= row.cellIterator();
                cellNo          = 0;
                binno           = "";
                longUrl         = "";
                shortUrl        = "";
                
                cell = row.getCell(4);
                if(cell!=null && cell.getStringCellValue()!=null){
                    binno = cell.getStringCellValue();
                }
                
                if(binno!=null && binno.length()>5 ){
                    result = postRequestV3(binno);
                    System.out.println("rownum..:" + rownum + " binno..:" + binno + " result..:" + result.length);
                    if(result!=null){
                        row.getCell(5).setCellValue(checkNull(result[0],""));
                        row.getCell(6).setCellValue(checkNull(result[1],""));
                        row.getCell(7).setCellValue(checkNull(result[2],""));
                    }
                }
                
            }
            
            System.out.println("rownum..:" + rownum);
            fis.close();
            FileOutputStream out =  new FileOutputStream(folder+"newfile/"+sourceFileName);
            workbook.write(out);
            out.close();
            
            workbook.close();
            workbook = null;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(fis!=null)try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(BinlistReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            fis = null;
        }
        
    }
}
