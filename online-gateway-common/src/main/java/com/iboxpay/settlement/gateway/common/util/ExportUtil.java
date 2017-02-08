package com.iboxpay.settlement.gateway.common.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 导出Excel工具类
 * @author caolipeng
 * @date 2015年6月3日 上午8:56:15
 * @Version 1.0
 */
public class ExportUtil {
	private HSSFWorkbook wb = null;  
	private HSSFSheet sheet = null;  
    /** 
     * @param wb 
     * @param sheet 
     */  
	public ExportUtil(HSSFWorkbook wb, HSSFSheet sheet){  
        this.wb = wb;  
        this.sheet = sheet;  
    }  
    /** 
     * 合并单元格后给合并后的单元格加边框 
     * @param region 
     * @param cs 
     */  
    public void setRegionStyle(CellRangeAddress region, HSSFCellStyle cs)  
    {  
  
        int toprowNum = region.getFirstRow();  
        for (int i = toprowNum; i <= region.getLastRow(); i++)  
        {  
            HSSFRow row = sheet.getRow(i);  
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++)  
            {  
                HSSFCell cell = row.getCell(j);  
                cell.setCellStyle(cs);  
            }  
        }  
    }  
    /** 
     * 设置表头的单元格样式 
     * @return 
     */  
    public HSSFCellStyle getHeadStyle(){  
        // 创建单元格样式  
        HSSFCellStyle cellStyle = wb.createCellStyle();
        sheet.setDefaultColumnWidth(20);// 设置每列默认宽度
        // 设置单元格的背景颜色为淡蓝色  
        cellStyle.setFillForegroundColor(HSSFColor.PALE_BLUE.index);  
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);  
        // 设置单元格居中对齐  
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        // 设置单元格垂直居中对齐  
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 创建单元格内容显示不下时自动换行  
        cellStyle.setWrapText(true);  
        // 设置单元格字体样式  
        HSSFFont font = wb.createFont();  
        // 设置字体加粗  
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);  
        font.setFontName("宋体");  
        font.setFontHeight((short) 200);  
        cellStyle.setFont(font);  
        // 设置单元格边框为细线条  
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        return cellStyle;  
    }  
  
    /** 
     * 设置表体的单元格样式 
     *  
     * @return 
     */  
    public HSSFCellStyle getBodyStyle(){  
        // 创建单元格样式  
        HSSFCellStyle cellStyle = wb.createCellStyle(); 
        sheet.setDefaultColumnWidth(20);// 设置每列默认宽度
        // 设置单元格居中对齐  
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);  
        // 设置单元格垂直居中对齐  
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);  
        // 创建单元格内容显示不下时自动换行  
        cellStyle.setWrapText(true);  
        // 设置单元格字体样式  
        HSSFFont font = wb.createFont();  
        // 设置正常字体
        font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);  
        font.setFontName("宋体");  
        font.setFontHeight((short) 200);  
        cellStyle.setFont(font);  
        // 设置单元格边框为细线条  
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);  
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);  
        return cellStyle;  
    }  
    
    /**
	 * 设置工作表中某行某列的值
	 * @param row         行
	 * @param columnIndex 列数，从0开始
	 * @param value       某个单元格的值
	 */
	public void setCellValue(HSSFRow row,int columnIndex,String value) {
		//在索引0的位置创建单元格（左上端） 　　
		HSSFCell cell = row.createCell(columnIndex);
		//定义单元格为字符串类型 　　
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		//在单元格中输入一些内容 　　
		cell.setCellValue(value);
	}
	
	/**
	 * 带样式的设置工作表中某行某列的值
	 * @param row         行
	 * @param columnIndex 列数，从0开始
	 * @param value       某个单元格的值
	 * @param style       单元格的样式
	 */
	public void setCellValue(HSSFRow row,int columnIndex,String value,HSSFCellStyle style) {
		//在索引0的位置创建单元格（左上端） 　　
		HSSFCell cell = row.createCell(columnIndex);
		//定义单元格为字符串类型 　　
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellStyle(style);//设置单元格样式
		//在单元格中输入一些内容 　　
		cell.setCellValue(value);
	}
	
	/**
	 * 指定工作表中第几行第几个单元格的元素
	 * @param sheet       被读取的单元格
	 * @param rowIndex    第几行，从0开始
	 * @param cellnum     第几个单元格，从0开始
	 */
	public String getCellValue(HSSFSheet sheet,int rowIndex,int cellnum){
		HSSFRow row = sheet.getRow(rowIndex);// 第几行(row)
		HSSFCell cell = row.getCell(cellnum);//第几个单元格(cell)
		String cellValue = cell==null?"":cell.getStringCellValue();
		return cellValue;
	}
}
