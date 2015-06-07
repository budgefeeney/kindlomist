package org.feenaboccles.kindlomist.run;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.Charsets;
import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.markdown.EconomistWriter;
import org.feenaboccles.kindlomist.download.Downloader;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

/**
 * The main entry point for the command-line app. Processes commandline
 * arguments and then executes the script accordingly.
 */
public class Main {
	private static final int EXIT_SUCCESS = 0;
	private static final int EXIT_FAILURE = -1;
	private static final String SAMPLE_LAUNCH_CMD = "java Main command [options...]\n"
			+ "   Specify a path to use locally stored files instead of the online edition\n"
			+ "   Use the --download-only option to download without converting.\n\n";

	public static final void main(String[] args) {
		System.exit(new Main().call(args));
	}

	private boolean   showHelp  = false;
	private DateStamp dateStamp = null;
	private UserName  username  = null;
	private Password  password  = null;
	private String    passwordText  = null;
	private Path      path          = null;
	private Path      pandocPath    = null;


	/**
	 * Processes the commandline arguments and executed the appropriate action.
	 */
	public Integer call(String[] args) {
		try {
			parseArguments(args);

			// Download the given issue of the Economist
			Downloader d = new Downloader(dateStamp, username, password);
			Economist economistIssue = d.call();

			// Write that issue to a temporary file in Markdown format
			Path mdPath = Files.createTempFile("economist-" + dateStamp.value(), ".md");
			mdPath.toFile().deleteOnExit();

			try (BufferedWriter wtr = Files.newBufferedWriter(mdPath, Charsets.UTF_8)) {
				EconomistWriter ewtr = new EconomistWriter();
				ewtr.writeEconomist(wtr, economistIssue);
			}

			// Use Pandoc to convert the Markdown file to an epub file.
			if (! path.getFileName().toString().toLowerCase().endsWith(".epub"))
				path = Paths.get(path.toString() + ".epub");

			Path coverImagePath = economistIssue.getImages().getImage(economistIssue.getCoverImage());
			String command =
				pandocPath.toString()      + ' '
				+ "-S"                     + ' '
				+ "--epub-chapter-level 1" + ' '
				+ "--toc --toc-depth 2"    + ' '
				+ "-o " + path.toString()  + ' '
				+ "--epub-cover-image " + coverImagePath.toString() + ' '
				+ mdPath.toString();

			shellExecAndWait(command);
			return EXIT_SUCCESS;

		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			return EXIT_FAILURE;
		}
	}

	/**
	 * Extracts the given shell command and waits for it to execute
	 */
	public static final void shellExecAndWait (String command) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process p  = rt.exec(command);
		p.waitFor();
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

			if (!Files.exists(path)) {
				throw new IllegalStateException("The given folder does not exist : " + path);
			} else if (!Files.isDirectory(path)) {
				throw new IllegalStateException("The given folder path points to a file, not a folder: " + path);
			}

			if (dateStamp == null) {
				throw new IllegalStateException("Need to specify the date-stamp");
			}
			if (username == null)
				throw new IllegalStateException("Need to provide a username when downloading files");
			if (StringUtils.isBlank(passwordText))
				throw new IllegalStateException("Need to provide a password when downloading files");

			Path passwordFile = Paths.get(passwordText);
			if (Files.exists(passwordFile)) {
				passwordText = Files.readAllLines(passwordFile).get(0);
			}
			password = Password.of(passwordText);

			if (pandocPath == null)
				pandocPath = guessPandocPath();

			if (! Files.exists(pandocPath)) {
				throw new IllegalArgumentException("There is not pandoc executable at the given path: " + pandocPath.toString());
			}
			if (! Files.isExecutable(pandocPath)) {
				throw new IllegalArgumentException("The path to the 'pandoc' program points to a file that is not actually executable : '" + pandocPath.toString() + "'");
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println(SAMPLE_LAUNCH_CMD);

			// print the list of available options
			parser.printUsage(System.err);
		}
	}


	private final Path guessPandocPath() throws IOException {
		List<Path> paths = new ArrayList<>(5);
		paths.add (Paths.get(System.getProperty("user.home"))
						.resolve(".cabal")
						.resolve("bin")
						.resolve("pandoc"));
		paths.add (Paths.get("/usr/bin/pandoc"));
		paths.add (Paths.get("/usr/local/bin/pandoc"));
		paths.add (Paths.get("/opt/local/bin/pandoc"));
		paths.add (Paths.get("C:/Program Files/Pandoc/pandoc"));

		for (Path path : paths) {
			if (Files.exists(path)) {
				return path;
			}
		}

		// Failed to find, format an error message enumerating the locations we checked.
		String pathsStr = paths.stream()
				.map(Path::toString)
				.collect(Collectors.joining(", "));
		throw new IOException("Cannot find pandoc in any of the following paths: " + pathsStr);
	}

	public boolean isShowHelp() {
		return showHelp;
	}

	@Option(name = "-h", aliases = "--help", usage = "Show this help message.", metaVar = " ")
	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}

	public String getDateStamp() {
		return dateStamp.toString();
	}

	@Option(name = "-d", aliases = "--date", usage = "The date identifying the issue to convert, in yyyy-mm-dd format", metaVar = " ")
	public void setDateStamp(String dateStamp) {
		this.dateStamp = DateStamp.of(dateStamp);
	}

	public String getUsername() {
		return username.toString();
	}

	@Option(name = "-u", aliases = "--username", usage = "The username to log into the site with", metaVar = " ")
	public void setUsername(String username) {
		this.username = UserName.of(username);
	}

	public String getPassword() {
		return passwordText;
	}

	@Option(name = "-p", aliases = "--password", usage = "The password to log into the site with, or a path to a file containing the password", metaVar = " ")
	public void setPassword(String password) {
		this.passwordText = password;
	}

	public String getPath() {
		return path.toString();
	}

	@Option(name = "-o", aliases = "--out-file", usage = "Where the resulting epub file should be saved", metaVar = " ")
	public void setPath(@NonNull String path) {
		this.path = Paths.get(path.trim());
	}

	public Path getPandocPath() {
		return pandocPath;
	}

	@Option(name = "-b", aliases = "--pandoc-path", usage = "The path where the 'pandoc' program may be found", metaVar = " ")
	public void setPandocPath(Path pandocPath) {
		this.pandocPath = pandocPath;
	}
}
