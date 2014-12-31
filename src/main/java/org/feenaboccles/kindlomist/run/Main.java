package org.feenaboccles.kindlomist.run;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.download.Downloader;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

/**
 * The main entry point for the command-line app. Processes commandline
 * arguments and then executes the script accordingly.
 */
public class Main {
	private static final String SAMPLE_LAUNCH_CMD = "java Main command [options...]\n"
			+ "   Specify a path to use locally stored files instead of the online edition\n"
			+ "   Use the --download-only option to download without converting.\n\n";

	public static final void main(String[] args) {
		new Main().call(args);
	}

	private boolean showHelp = false;
	private String dateStamp = null;
	private String username = null;
	private String password = null;
	private String pathStr = null;
	private boolean downloadOnly = false;

	private Path path = null;
	private boolean downloadRequired = true;

	/**
	 * Processes the commandline arguments and executed the appropriate action.
	 */
	public void call(String[] args) {
		try {
			parseArguments(args);
			
			final Economist issue;
			if (downloadRequired) {
				issue = new Downloader(dateStamp, username, password).call();
			}
			else {
				try (InputStream istream = new BufferedInputStream (Files.newInputStream(path))) {
					issue = SerializationUtils.deserialize(istream);
				}
			}

		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
		}
	}

	/**
	 * Parses the arguments and performs sanity checks on them. If the input is
	 * invalid this will show an error message, help, and then quit the app. If
	 * the showHelp option (i.e. --help) is turned on, this will print the the
	 * help message, and quit the app.
	 */
	private void parseArguments(String[] args) {
		ParserProperties props = ParserProperties.defaults();
		CmdLineParser parser = new CmdLineParser(this, props);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// See if they wanted to see the help message.
			if (showHelp) {
				System.out.println(SAMPLE_LAUNCH_CMD);
				parser.printUsage(System.out);
				System.exit(0);
			}

			// sanity checks
			pathStr = StringUtils.trimToEmpty(pathStr);
			if (!pathStr.isEmpty())
				path = Paths.get(pathStr);

			if (!Files.exists(path)) {
				throw new IllegalStateException("The given folder does not exist : " + path);
			} else if (!Files.isDirectory(path)) {
				throw new IllegalStateException("The given folder path points to a file, not a folder: " + path);
			}

			if (downloadOnly) {
				if (path == null) {
					throw new IllegalStateException("Need to specify a folder to which files should be downloaded");
				}
				if (dateStamp == null) {
					throw new IllegalStateException("Need to specify the date-stamp");
				}
			} else if (!downloadOnly) {
				if (path == null) {
					if (dateStamp == null) {
						throw new IllegalStateException("Need to provide either a date-stamp, or a path to a folder of downloaded files.");
					} else {
						if (username == null)
							throw new IllegalStateException("Need to provide a username when downloading files");
						if (password == null)
							throw new IllegalStateException("Need to provide a password when downloading files");
					}
				}
			}
			
			downloadRequired = ! downloadOnly && path == null;

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println(SAMPLE_LAUNCH_CMD);

			// print the list of available options
			parser.printUsage(System.err);
		}
	}

	public boolean isShowHelp() {
		return showHelp;
	}

	@Option(name = "-h", aliases = "--help", usage = "Show this help message.", metaVar = " ")
	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}

	public String getDateStamp() {
		return dateStamp;
	}

	@Option(name = "-d", aliases = "--date", usage = "The date identifying the issue to convert, in yyyy-mm-dd format", metaVar = " ")
	public void setDateStamp(String dateStamp) {
		this.dateStamp = dateStamp;
	}

	public String getUsername() {
		return username;
	}

	@Option(name = "-u", aliases = "--username", usage = "The username to log into the site with", metaVar = " ")
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	@Option(name = "-p", aliases = "--password", usage = "The password to log into the site with", metaVar = " ")
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		return pathStr;
	}

	@Option(name = "-f", aliases = "--folder", usage = "The folder in which to save or load the HTML files from the Economist", metaVar = " ")
	public void setPath(String path) {
		this.pathStr = path;
	}

	public boolean isDownloadOnly() {
		return downloadOnly;
	}

	@Option(name = "-m", aliases = "--download-only", usage = "Only download the files, don't convert them. Requires the path be set.")
	public void setDownloadOnly(boolean downloadOnly) {
		this.downloadOnly = downloadOnly;
	}

}
