package org.feenaboccles.kindlomist.run;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

/**
 * The main entry point for the command-line app. Processes commandline
 * arguments and then executes the script accordingly.
 */
public class Main
{
	private static final String SAMPLE_LAUNCH_CMD = "java Main command [options...]";

	public static final void main (String[] args) {
		new Main().call(args);
	}
	
	private boolean showHelp = false;
	private String dateStamp = null;
	private String username  = null;
	private String password  = null;
	
	public void call(String[] args)
	{	try
		{	parseArguments(args);
		}
		catch (Exception e)
		{	System.err.println ("ERROR: " + e.getMessage());
		}
	}
	
	private void parseArguments(String[] args) throws CmdLineException
	{
		ParserProperties props = ParserProperties.defaults();
		CmdLineParser parser = new CmdLineParser(this, props);

        try {
            // parse the arguments.
            parser.parseArgument(args);
            
            if (showHelp) {
            	System.out.println(SAMPLE_LAUNCH_CMD);
            	parser.printUsage(System.out);
            	System.exit(0);
            }

        }
        catch (Exception e)
        {	System.err.println(e.getMessage());
            System.err.println(SAMPLE_LAUNCH_CMD);
            
            // print the list of available options
            parser.printUsage(System.err);
        }
	}
	
	public boolean isShowHelp() {
		return showHelp;
	}
	
	@Option(name="-h", aliases="--help", usage="Show this help message.", metaVar=" ")
	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}
	
	public String getDateStamp() {
		return dateStamp;
	}
	
	@Option(name="-d", aliases="--date", usage="The date identifying the issue to convert, in yyyy-mm-dd format", metaVar=" ")
	public void setDateStamp(String dateStamp) {
		this.dateStamp = dateStamp;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Option(name="-u", aliases="--username", usage="The username to log into the site with", metaVar=" ")
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	@Option(name="-p", aliases="--password", usage="The password to log into the site with", metaVar=" ")
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
}
