/*
 * Copyright 2013 Sourabh Ghorpade

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/   
package com.factorialCalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class CalculateFactorial extends Activity 
{
	private static final String NUMBER = "NUMBER";
	private static final String FACTORIAL = "FACTORIAL";
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_factorial);
        Intent intent=getIntent();
        double number=intent.getDoubleExtra(NUMBER,0);
        double factorialResult=calculateFactorialOfANumber(number);
        Intent resultData=new Intent();
        resultData.putExtra(FACTORIAL,factorialResult);
        setResult(RESULT_OK,resultData);
        Log.v(FACTORIAL,""+ factorialResult);
        finish();
        
    }
    public double calculateFactorialOfANumber(double number) 
	{
    	if(number==0)
    		return 1;
		return number * calculateFactorialOfANumber(number-1);
   	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_calculate_factorial, menu);
        return true;
    }
}
