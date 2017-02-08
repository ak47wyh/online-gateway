package com.iboxpay.settlement.gateway.common.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.dao.DetailDao;
import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.ExportUtil;
/**
 * 交易明细service
 * @author caolipeng
 * @date 2015年6月3日 上午9:13:15
 * @Version 1.0
 */
@Service
public class DetailService {

	@Resource
    private DetailDao detailDaoImpl;
	/**
	 * 导出excel
	 * @param accNo
	 * @param beginDateStr
	 * @param endDateStr
	 * @param titles
	 * @param outputStream
	 */
	public void exportExcel(String accNo,Date beginDateStr,Date endDateStr,
			String[] titles, ServletOutputStream outputStream){  
		List<DetailEntity> list = detailDaoImpl.getDetailList(accNo,beginDateStr,endDateStr);  
        // 创建一个workbook 对应一个excel应用文件  
        HSSFWorkbook workBook = new HSSFWorkbook();  
        // 在workbook中添加一个sheet,对应Excel文件中的sheet  
        HSSFSheet sheet = workBook.createSheet("导出excel");  
        ExportUtil exportUtil = new ExportUtil(workBook, sheet);  
        HSSFCellStyle headStyle = exportUtil.getHeadStyle();  
        HSSFCellStyle bodyStyle = exportUtil.getBodyStyle();  
        // 构建表头  
        HSSFRow headRow = sheet.createRow(0);  
        HSSFCell cell = null;  
        for (int i = 0; i < titles.length; i++){  
            cell = headRow.createCell(i);  
            cell.setCellStyle(headStyle);  
            cell.setCellValue(titles[i]);  
        }  
        // 构建表体数据  
        if (list != null && list.size() > 0){  
            for(int j = 0; j < list.size(); j++){  
                HSSFRow bodyRow = sheet.createRow(j + 1);  
                DetailEntity detailEntity = list.get(j);  
                
                setHSSFCell(bodyRow, 0, bodyStyle,String.valueOf(detailEntity.getId()));//ID
                setHSSFCell(bodyRow, 1, bodyStyle,detailEntity.getAccNo());//公司账号
                setHSSFCell(bodyRow, 2, bodyStyle,detailEntity.getCustomerAccNo());//对方账号
                setHSSFCell(bodyRow, 3, bodyStyle,detailEntity.getCustomerAccName());//对方账户户名
                setHSSFCell(bodyRow, 4, bodyStyle,detailEntity.getCustomerBankFullName());//对方银行
                setHSSFCell(bodyRow, 5, bodyStyle,detailEntity.getDebitAmount()==null?"":detailEntity.getDebitAmount().toString());//借方金额
                setHSSFCell(bodyRow, 6, bodyStyle,detailEntity.getCreditAmount()==null?"":detailEntity.getCreditAmount().toString());//贷方金额
                setHSSFCell(bodyRow, 7, bodyStyle,detailEntity.getBalance()==null?"":detailEntity.getBalance().toString());//余额
                setHSSFCell(bodyRow, 8, bodyStyle,DateTimeUtil.format(detailEntity.getTransDate(), "yyyy-MM-dd HH:mm:ss"));//交易时间
                setHSSFCell(bodyRow, 9, bodyStyle,detailEntity.getRemark());//备注
                setHSSFCell(bodyRow, 10, bodyStyle,detailEntity.getBankBatchSeqId());//银行批次流水
                setHSSFCell(bodyRow, 11, bodyStyle,detailEntity.getUseCode());//用途代码
                setHSSFCell(bodyRow, 12, bodyStyle,detailEntity.getUseDesc());//用途描述
                setHSSFCell(bodyRow, 13, bodyStyle,DateTimeUtil.format(detailEntity.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));//查询更新时间
            }  
        }  
        try{  
            workBook.write(outputStream);  
            outputStream.flush();  
            outputStream.close();  
        }catch (IOException e){  
            e.printStackTrace();  
        }finally{  
            try{  
                outputStream.close();  
            }catch (IOException e){  
                e.printStackTrace();  
            }  
        }  
    }
	/**
	 * 设置每个单元格
	 * @param bodyRow
	 * @param column
	 * @param bodyStyle
	 * @param cellValue
	 */
	private void setHSSFCell(HSSFRow bodyRow,int column,HSSFCellStyle bodyStyle,String cellValue){
		HSSFCell cell = bodyRow.createCell(column);
		cell.setCellStyle(bodyStyle);  
        cell.setCellValue(cellValue);  
	}
}
