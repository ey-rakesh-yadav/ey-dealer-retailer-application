# Udaan
CRM

Steps of setting up hyrbis project in our local system:

1)Extract the zip file of the hybris project setup.

2)Delete the data folder in the "C:\Work\projects\CXCOMM\hybris\" directory (Consider your hybris Setup directory)

3)Make sure the local.properties file in the "C:\Work\projects\CXCOMM\hybris\config" (Consider your hybris Setup directory) directory is upto date with the local.properties file in the dev branch.

4)Repeat the step 3 for the local-common.properties in the 'C:\Work\SCL\crm-backend\core-customize\hybris\config\environments' (Clone Projects folder).

5) Comment down the below lines of code in the localextensions.xml file in the 'C:\Work\projects\CXCOMM\hybris\config' directory (Consider your hybris Setup directory)
		
	<extension name="sclsamlsinglesignon"/>
    <extension name="samlsinglesignon"/>

6)Delete the custom folder in the "C:\Work\projects\CXCOMM\hybris\bin\" directory. (Consider your hybris Setup directory)

7)Make a junction 
 a) Execute this command 'mklink /J <Your-hybris-setup-folder\hybris\bin\custom> <Your-clone-project-folder\core-customize\hybris\bin\custom>'
	Example: 'mklink /J C:\Work\projects\CXCOMM\hybris\bin\custom C:\Work\SCL\crm-backend\core-customize\hybris\bin\custom' in the cmd prompt.
	
 b) The custom folder in the 'C:\Work\projects\CXCOMM\hybris\bin\' (Consider your hybris Setup directory) will be created automatically after the above command is executed.
 
8) Open cmd and change the directory to 'C:\Work\projects\CXCOMM\hybris\bin\platform'. (Consider your hybris Setup directory)
9) Type 'setantenv.bat' and enter.
10) Run 'ant clean all' command to build the project.
11) Once the build is successful, turn up the server using 'hybrisserver.bat debug' command.
12) Once the server is up, Initialize the system on HAC 
   a) Go to HAC -> Platform -> click on Initialization tab.
   b) Initilize by unselecting apparal , powertools and electronic store and click on initialize button.
