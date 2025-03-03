package com.sky.test;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

/**
 * 测试ApachePOI
 *
 * @author 王天一
 * @version 1.0
 */
@SpringBootTest
public class ApachePOITest {
    //写
    public static void write() throws Exception {
        //在内存中   创建excel文件
        XSSFWorkbook excel = new XSSFWorkbook();
        //默认没有sheet  需要创建一个sheet
        XSSFSheet sheet = excel.createSheet("sheet1");
        //创建行  1代表第二行
        XSSFRow row = sheet.createRow(1);
        //创建第二行的第二个单元格
        XSSFCell cell = row.createCell(1);
        cell.setCellValue("hello");
        row.createCell(2).setCellValue("world");
        XSSFRow row1 = sheet.createRow(2);
        row1.createCell(1).setCellValue("hello");
        row1.createCell(2).setCellValue("excel");
        //使用文件输出流将内存中的文件输出到磁盘
        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\lenovo\\Desktop\\test.xlsx"));
        excel.write(fileOutputStream);

        excel.close();
        fileOutputStream.close();
    }
    //读
    public static void read() throws IOException {
        //读取磁盘上的文件到内存
        XSSFWorkbook excel = new XSSFWorkbook(new FileInputStream(new File("D:\\lenovo\\Desktop\\test.xlsx")));
        //用下标获取sheet对象
        XSSFSheet sheet = excel.getSheetAt(0);
        //获取最后一行的行号
        int lastRowNum = sheet.getLastRowNum();
        //遍历获取内容
        for (int i=1;i<=lastRowNum;i++){
            XSSFRow row = sheet.getRow(i);
            String value1 = row.getCell(1).getStringCellValue();
            String value2 = row.getCell(2).getStringCellValue();
            System.out.println(value1+value2);
        }
        excel.close();
    }//感觉没什么用  因为是照着答案读文件

    //不知道为什么这么调就可以   用@Test运行会报错
    public static void main(String[] args) throws Exception {
        write();
        read();
    }
}
