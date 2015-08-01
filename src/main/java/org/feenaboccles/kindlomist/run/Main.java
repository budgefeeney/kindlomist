package org.feenaboccles.kindlomist.run;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.Charsets;
import org.feenaboccles.kindlomist.articles.Economist;
import org.feenaboccles.kindlomist.articles.markdown.EconomistWriter;
import org.feenaboccles.kindlomist.download.DateStamp;
import org.feenaboccles.kindlomist.download.Downloader;
import org.feenaboccles.kindlomist.download.Password;
import org.feenaboccles.kindlomist.download.Email;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

/**
 * The main entry point for the command-line app. Processes commandline
 * arguments and then executes the script accordingly.
 */
@Log4j2
public class Main {
	private static final int EXIT_SUCCESS = 0;
	private static final int EXIT_FAILURE = -1;
	private static final String SAMPLE_LAUNCH_CMD = "java Main command [options...]\n"
			+ "  If the date is omitted, the most recent issue is downloaded\n"
			+ "  If an output folder is specified intead of an output file, the \n"
			+ "  name economist-<datestamp>.epub is used instead.\n";

	public static void main(String[] args) {
		System.exit(new Main().call(args));
	}

	private boolean   showHelp  = false;
	private DateStamp dateStamp = null;
	private Email     userEmail = null;
	private Password  password  = null;
	private String    passwordText  = null;
	private Path      passwordPath  = null;
	private Path      path          = null;
	private Path      pandocPath    = null;
	private Path      kindleGenPath = null;


	/**
	 * Processes the commandline arguments and executed the appropriate action.
	 *
	 * Returns integer values indicating a successful or failed run, using the
	 * usual Unix / C conventions (zero is success, non-zero is failure)
	 */
	public Integer call(String[] args) {
		try {
			parseArguments(args);

			// Download the given issue of the Economist
			Downloader d = new Downloader(dateStamp, userEmail, password);
			Economist economistIssue = d.call();

			// Write that issue to a temporary file in Markdown format
			Path mdPath = Files.createTempFile("economist-" + dateStamp.value(), ".md");
			mdPath.toFile().deleteOnExit();

			try (BufferedWriter wtr = Files.newBufferedWriter(mdPath, Charsets.UTF_8)) {
				EconomistWriter.write(wtr, economistIssue);
			}

			// Use Pandoc to convert the Markdown file to an epub file. Make sure we've
			ensurePathHasEpubExt();
			Path coverImagePath = economistIssue.getPathToCoverImage();
			convertMarkdownToEpub(mdPath, coverImagePath);

			// Use KindleGen to convert to a Mobi file.
			if (kindleGenPath != null) {
				replaceEpubWithMobi(path);
			}
			return EXIT_SUCCESS;

		} catch (Exception e) {
			log.error ("Error occurred while running the program : " + e.getMessage(), e);
			System.err.println("ERROR: " + e.getMessage());
			return EXIT_FAILURE;
		}
	}

	private void ensurePathHasEpubExt() {
		final String pathStr = path.toString();
		final String lwrPathStr = pathStr.toLowerCase();
		if (! lwrPathStr.endsWith(".epub")) {
            if (lwrPathStr.endsWith(".mobi")) {
                path = Paths.get(pathStr.substring(0, pathStr.length() - 4) + ".epub");
            } else {
                path = Paths.get(pathStr + ".epub");
            }
        }
	}

	/**
	 * Uses the KindleGen executable to convert the epub file to a Mobi
	 * file, and, if the conversion succeeded, delete the epub file.
	 * @param epubPath the path to the ePub file - the epub file is
	 *               the same except the .md extension is replaced with .epub
	 */
	private void replaceEpubWithMobi(Path epubPath) throws IOException, InterruptedException {
		String epubPathStr = epubPath.toString();
		String kCommand = kindleGenPath.toString() + ' ' + epubPath;

		shellExecAndWait(kCommand);

		Path mobiPath = Paths.get(epubPathStr.replace(".epub", ".mobi"));
		if (Files.exists(mobiPath) && Files.size(mobiPath) > 0) {
            Files.delete(epubPath);
        } else {
			throw new IOException ("Failed to convert the epub file to a MOBI file");
		}
	}

	/**
	 * Converts the given markdown file to an epub files with the given cover
	 * images using pandoc
	 */
	private void convertMarkdownToEpub(Path mdPath, Path coverImagePath) throws IOException, InterruptedException {
		String command =
            pandocPath.toString()      + ' '
            + "-S"                     + ' '
            + "--epub-chapter-level 1" + ' '
            + "--toc --toc-depth 2"    + ' '
            + "-o " + path.toString()  + ' '
            + "--epub-cover-image " + coverImagePath.toString() + ' '
            + mdPath.toString();

		shellExecAndWait(command);
	}

	/**
	 * Extracts the given shell command and waits for it to execute
	 */
	public static void shellExecAndWait (String command) throws IOException, InterruptedException {
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

			if (dateStamp == null) {
				dateStamp = dateOfMostRecentIssue();
				log.info("No date specified, using date of most recent issue - " + dateStamp);
			}


			if (Files.exists(path)) {
				if (Files.isDirectory(path)) {
					path = path.resolve("economist-" + dateStamp + ".epub");
					log.info ("An output folder was supplied instead of an output file, generating a filename from the datestamp");
				} else {
					throw new IllegalArgumentException("Another file already exists with the name " + path);
				}
			} else if (! Files.exists(path.getParent())) {
				throw new IllegalArgumentException("Invalid output-file name, the parent directory does not exist : " + path);
			}

			if (userEmail == null)
				throw new IllegalStateException("Need to provide a username when downloading files");

			if (passwordText != null && passwordPath != null)
				throw new IllegalStateException("You've provided both a password and a password path, please provide only one");
			if (passwordText == null && passwordPath == null)
				throw new IllegalStateException("You've provided neither a password and a password path, please provide one of the two");

			if (passwordText != null) {
				password = Password.of(passwordText);
			} else {
				if (! Files.exists(passwordPath)) {
					throw new IllegalArgumentException("No password file exists at the given path " + path);
				} else {
					password = Password.of(Files.readAllLines(passwordPath).get(0));
				}
			}

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
			System.exit(EXIT_FAILURE);
		}
	}

	private DateStamp dateOfMostRecentIssue() {
		// The most recent issue is published by 11pm British Time each Thursday
		ZoneId timeZone = ZoneId.of("Europe/London");
		ZonedDateTime londonTime = ZonedDateTime.now(timeZone);

		return DateStamp.of(DateStamp.maxDateTime(londonTime));
	}


	private Path guessPandocPath() throws IOException {
		List<Path> paths = new ArrayList<>(5);
		paths.add (Paths.get(System.getProperty("user.home"))
						.resolve(".cabal")
						.resolve("bin")
						.resolve("pandoc"));
		paths.add (Paths.get("/usr/bin/pandoc"));
		paths.add (Paths.get("/usr/local/bin/pandoc"));
		paths.add (Paths.get("/opt/local/bin/pandoc"));
		paths.add (Paths.get("C:/Program Files/Pandoc/pandoc"));
		paths.add (Paths.get("C:/Program Files/Pandoc/bin/pandoc"));

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

	@Option(name = "-d", aliases = "--date", usage = "The date identifying the issue to convert, in yyyy-mm-dd format.", metaVar = " ")
	public void setDateStamp(String dateStamp) {
		this.dateStamp = DateStamp.of(dateStamp);
	}

	public String getUserEmail() {
		return userEmail.toString();
	}

	@Option(name = "-u", aliases = "--username", usage = "The username to log into the Economist website", metaVar = " ", required=true)
	public void setUserEmail(String userEmail) {
		this.userEmail = Email.of(userEmail);
	}

	public String getPassword() {
		return passwordText;
	}

	@Option(name = "-p", aliases = "--password", usage = "The password used to log into the Economist website", metaVar = " ")
	public void setPassword(String password) {
		this.passwordText = password;
	}


	public String getPasswordPath() {
		return passwordPath.toString();
	}

	@Option(name = "-f", aliases = "--password-file", usage = "The path to a file containing the password used to log into the Economist website", metaVar = " ")
	public void setPasswordPath(String passwordPath) {
		this.passwordPath = Paths.get(passwordPath);
	}

	public String getPath() {
		return path.toString();
	}

	@Option(name = "-o", aliases = "--out-file", usage = "Where the resulting epub file should be saved. Can be a folder or a filename.", metaVar = " ", required=true)
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

	public Path getKindleGenPath() {
		return kindleGenPath;
	}

	@Option(name = "-k", aliases = "--kindle-gen-path", usage = "The path to the kindlegen executable. If present, used to convert the epub file" , metaVar = " ")
	public void setKindleGenPath(Path kindleGenPath) {
		this.kindleGenPath = kindleGenPath;
	}
}
