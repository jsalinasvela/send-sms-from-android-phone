public void sms(final String phoneNumber, final String message) {

    String SENT = "SMS_SENT";
    
    Log.v("Linea 122", "sms('"+phoneNumber+"','"+message+"')");

    PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
    


    registerReceiver(new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //following line avoids that sms's are accumulated next time that a sms needs to be sent
            context.unregisterReceiver(this);

            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                   Log.v("Linea 122", "sms(...) -> registerReceiver(..) -> Error code: "+getResultCode());

                    //Toast.makeText(getBaseContext(), "Radio OFF", Toast.LENGTH_SHORT).show();
                    //Execute function to deal with error
                    handleSmsError("Error code: "+getResultCode(), phoneNumber, message);
                    break;
            }

        }
    }, new IntentFilter(SENT));

    SmsManager smsManager = SmsManager.getDefault();

    smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
    report(phoneNumber);
}



public void handleSmsError(final String smsError, final String phoneNumber, final String message){
    //Send API request to re-send the sms with another number
    //Instantiate the RequestQueue
    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
    //Log.v("Linea 179","handleSmsError('"+smsError+"','"+phoneNumber+"','"+message+"')");
    //Log.v("RESEND MESSAGE: ", message);
    //Request a string response from the provided URL.
    StringRequest s = new StringRequest(Request.Method.POST, getString(/*API uri to send the request*/), new Response.Listener<StringRequest.reply>(){

        public void onResponse(StringRequest.reply result){
            Log.v("Linea 188", "onResponse(...) -> result.response: "+result.response);
            Log.v("Linea 188 error", smsError);
        }
    }, new Response.ErrorListener(){

        public void onErrorResponse(VolleyError error){
            Log.v("state VHTTP", "That didn work!");
        }
    }) {

        protected Map<String, String> getParams(){
            Map<String, String> params= new HashMap<>();
            //these are the parameters being sent as input to API uri
            params.put("From", getString(/*number from which sms is being sent*/));
            params.put("To", phoneNumber);
            params.put("Message", message);
            params.put("Error", smsError);
            return params;
        }

        public String getBodyContentType() {
            return "application/x-www-form-urlencoded; charset=UTF-8";
        }

        public Map<String, String> getHeaders(){
            Map<String, String> headers = new HashMap<>();

            headers.put("User-Agent", "BigSms/1.0");
            return headers;
        }
    };

    queue.add(s);
}



private void report(final String phoneNumber) {

    //THIS METHOD SENDS PARAMETERS TO THE API SO THAT IT CAN SAVE THE INFORMATION ABOUT THE SENT SMS IN THE DATABASE
    // Instantiate the RequestQueue.
    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

    // Request a string response from the provided URL.
    StringRequest s = new StringRequest(Request.Method.POST, getString(/*API uri to send the request*/), new Response.Listener<StringRequest.reply>() {

        @Override
        public void onResponse(StringRequest.reply result) {
            Log.v("RETURNED STATE report()", "Response: "+result.response);
        }

    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.v("STATE VHTTP", "That didn't work!");
        }
    }) {

        @Override
        protected Map<String, String> getParams() {

            Map<String, String> params = new HashMap<>();
            params.put("To", phoneNumber);
            params.put("Status", Calendar.getInstance().getTime().toString());
            params.put("MessageUUID", "");
            return params;
        }

        @Override
        public String getBodyContentType() {
            return "application/x-www-form-urlencoded; charset=UTF-8";
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            /*
            try {
                headers.put("Token", table.getString(Contract.Entry.COLUMN_TOKEN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            */

            headers.put("User-Agent", "BigSms/1.0");
            return headers;
        }

    };

    queue.add(s);
}