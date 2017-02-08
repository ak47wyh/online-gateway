<%@ page language="java" contentType="text/html; charset=UTF-8" isErrorPage="true" pageEncoding="UTF-8"%>
<html>
<body>
<h2>Hello World!</h2>


<form action="https://mapi.alipay.com/gateway.do" method="post">
<input type="text" name="_input_charset" value="UTF-8" size="32"><br/>
<input type="text" name="alipay_ca_request" value="2" size="32"><br/>
<input type="text" name="it_b_pay" value="10m" size="32"><br/>
<input type="text" name="notify_url" value="http://cbx01.sz.iboxpay.com/cashbox/alipay/qrtrade_callback.htm" size="32"><br/>
<input type="text" name="operator_code" value="1" size="32"><br/>
<input type="text" name="out_trade_no" value="99101413823381948713" size="32"><br/>
<input type="text" name="partner" value="2088511235449145" size="32"><br/>
<input type="text" name="product_code" value="QR_CODE_OFFLINE" size="32"><br/>
<input type="text" name="service" value="alipay.trade.precreate" size="32"><br/>
<input type="text" name="sign" value="7a1a82a786e5a3cfb0b73cf6a1293be0" size="32"><br/>
<input type="text" name="sign_type" value="MD5" size="32"><br/>
<input type="text" name="subject" value="99101413823381948713" size="32"><br/>
<input type="text" name="total_fee" value="0.01" size="32"><br/>
<input type="submit">
</form>
</body>
</html>
