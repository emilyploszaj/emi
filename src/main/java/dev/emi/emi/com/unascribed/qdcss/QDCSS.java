package dev.emi.emi.com.unascribed.qdcss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

/**
 * A quick-and-dirty "CSS" parser.
 */
public class QDCSS {
	public static class QDCSSException extends IllegalArgumentException {
		public QDCSSException() {}
		public QDCSSException(String message, Throwable cause) { super(message, cause); }
		public QDCSSException(String s) { super(s); }
		public QDCSSException(Throwable cause) { super(cause); }
	}
	public static class BadValueException extends QDCSSException {
		public BadValueException() {}
		public BadValueException(String message, Throwable cause) { super(message, cause); }
		public BadValueException(String s) { super(s); }
		public BadValueException(Throwable cause) { super(cause); }
	}
	public static class SyntaxErrorException extends QDCSSException {
		public SyntaxErrorException() {}
		public SyntaxErrorException(String message, Throwable cause) { super(message, cause); }
		public SyntaxErrorException(String s) { super(s); }
		public SyntaxErrorException(Throwable cause) { super(cause); }
	}

	private static class BlameString {
		public final String value;
		public final String file;
		public final int line;

		private BlameString(String value) {
			this(value, null, -1);
		}

		private BlameString(String value, String file, int line) {
			this.value = value;
			this.file = file;
			this.line = line;
		}
		public String blame() {
			return line == -1 ? "<unknown>" : "line "+line+" in "+file;
		}
	}

	private final String prelude;
	private final Map<String, List<BlameString>> data;

	private Consumer<String> yapLog;

	private QDCSS(String prelude, Map<String, List<BlameString>> data) {
		this.prelude = prelude;
		this.data = data;
	}

	/**
	 * Enables "yap" mode for parse failures in this config, where rather than throwing a
	 * BadValueException a warning string will be sent to this Consumer and an empty Optional
	 * returned to the caller of get*.
	 * <p>
	 * If yapLog is null, "yap" mode is turned off.
	 */
	public void setYapLog(Consumer<String> yapLog) {
		this.yapLog = yapLog;
	}

	public boolean containsKey(String key) {
		return data.containsKey(key) && !data.get(key).isEmpty();
	}

	public void put(String key, String value) {
		List<BlameString> li = new ArrayList<>();
		li.add(new BlameString(value));
		data.put(key, li);
	}

	public void put(String key, String... values) {
		List<BlameString> li = new ArrayList<>();
		for (String v : values) {
			li.add(new BlameString(v));
		}
		data.put(key, li);
	}

	public void put(String key, Iterable<String> values) {
		List<BlameString> li = new ArrayList<>();
		for (String v : values) {
			li.add(new BlameString(v));
		}
		data.put(key, li);
	}

	/**
	 * Return all defined values for the given key, or an empty list if it's not defined.
	 */
	public List<String> getAll(String key) {
		return unwrap(data.get(key));
	}

	private List<BlameString> getAllBlamed(String key) {
		return data.containsKey(key) ? data.get(key) : Collections.emptyList();
	}

	public String getBlame(String key) {
		return getBlamed(key).map(BlameString::blame).orElse("<unknown>");
	}

	public String getBlame(String key, int index) {
		if (containsKey(key)) {
			return getAllBlamed(key).get(index).blame();
		}
		return "<unknown>";
	}

	private List<String> unwrap(List<BlameString> list) {
		if (list == null) return Collections.emptyList();
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return unwrap(list.get(index));
			}

			@Override
			public int size() {
				return list.size();
			}

		};
	}

	private String unwrap(BlameString bs) {
		if (bs == null) return null;
		return bs.value;
	}

	/**
	 * Return the last defined value for the given key.
	 */
	public Optional<String> get(String key) {
		return Optional.ofNullable(getLast(getAll(key)));
	}

	private Optional<BlameString> getBlamed(String key) {
		return Optional.ofNullable(getLast(getAllBlamed(key)));
	}

	public Optional<Integer> getInt(String key) throws BadValueException {
		return getParsed(key, Integer::parseInt, () -> "a whole number");
	}

	public Optional<Double> getDouble(String key) throws BadValueException {
		return getParsed(key, Double::parseDouble, () -> "a number");
	}

	public Optional<Boolean> getBoolean(String key) throws BadValueException {
		return getParsed(key, this::strictParseBoolean, () -> "true/on or false/off");
	}

	private boolean strictParseBoolean(String s) {
		switch (s.toLowerCase(Locale.ROOT)) {
			case "on": case "true": return true;
			case "off": case "false": return false;
			default: throw new IllegalArgumentException();
		}
	}

	public <E extends Enum<E>> Optional<E> getEnum(String key, Class<E> clazz) throws BadValueException {
		return getParsed(key, s -> Enum.valueOf(clazz, s.toUpperCase(Locale.ROOT)), () -> {
			StringBuilder sb = new StringBuilder("one of ");
			boolean first = true;
			for (E e : clazz.getEnumConstants()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(e.name().toLowerCase(Locale.ROOT));
			}
			return sb.toString();
		});
	}

	private <T> Optional<T> getParsed(String key, Function<String, ? extends T> parser, Supplier<String> error) throws BadValueException {
		Optional<String> s = get(key);
		if (!s.isPresent()) return Optional.empty();
		try {
			return Optional.of(parser.apply(s.get()));
		} catch (IllegalArgumentException e) {
			String msg = key+" must be "+error.get()+" (got "+s.get()+") near "+getBlame(key);
			if (yapLog != null) {
				yapLog.accept(msg);
				return Optional.empty();
			} else {
				throw new BadValueException(msg, e);
			}
		}
	}

	private <T> T getLast(List<T> list) {
		return list == null || list.isEmpty() ? null : list.get(list.size()-1);
	}

	public Set<String> keySet() {
		return data.keySet();
	}

	public Set<Map.Entry<String, List<String>>> entrySet() {
		return new AbstractSet<Map.Entry<String, List<String>>>() {

			@Override
			public Iterator<Map.Entry<String, List<String>>> iterator() {
				Iterator<Map.Entry<String, List<BlameString>>> delegate = data.entrySet().iterator();
				return new Iterator<Map.Entry<String, List<String>>>() {

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public Map.Entry<String, List<String>> next() {
						Map.Entry<String, List<BlameString>> den = delegate.next();
						return new AbstractMap.SimpleImmutableEntry<>(den.getKey(), unwrap(den.getValue()));
					}
				};
			}

			@Override
			public int size() {
				return size();
			}

		};
	}

	public int size() {
		return data.size();
	}

	/**
	 * Lossily convert this QDCSS's data into an INI. Comments, section declarations, etc will
	 * be lost.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("; Loaded from ");
		sb.append(prelude);
		sb.append("\r\n");
		for (Map.Entry<String, List<String>> en : entrySet()) {
			for (String v : en.getValue()) {
				sb.append(en.getKey());
				sb.append("=");
				sb.append(v);
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Merge the given QDCSS's data with this QDCSS's data, returning a new QDCSS object. Keys
	 * defined in the given QDCSS will have their values appended to this one's. For usages of
	 * {@link #get}, this is equivalent to an override.
	 */
	public QDCSS merge(QDCSS that) {
		Map<String, List<BlameString>> newData = new LinkedHashMap<>(Math.max(this.size(), that.size()));
		newData.putAll(data);
		for (Map.Entry<String, List<BlameString>> en : that.data.entrySet()) {
			if (newData.containsKey(en.getKey())) {
				List<BlameString> merged = new ArrayList<>(newData.get(en.getKey()).size()+en.getValue().size());
				merged.addAll(newData.get(en.getKey()));
				merged.addAll(en.getValue());
				newData.put(en.getKey(), Collections.unmodifiableList(merged));
			} else {
				newData.put(en.getKey(), en.getValue());
			}
		}
		return new QDCSS(prelude+", merged with "+that.prelude, Collections.unmodifiableMap(newData));
	}

	/**
	 * Return a view of this QDCSS's data, dropping multivalues and collapsing to a basic key-value
	 * mapping that returns the last defined value for any given key.
	 */
	public Map<String, String> flatten() {
		return new AbstractMap<String, String>() {

			@Override
			public String get(Object key) {
				return QDCSS.this.get((String)key).orElse(null);
			}

			@Override
			public boolean containsKey(Object key) {
				return QDCSS.this.containsKey((String)key);
			}

			@Override
			public Set<String> keySet() {
				return QDCSS.this.keySet();
			}

			@Override
			public int size() {
				return QDCSS.this.size();
			}

			@Override
			public Set<Entry<String, String>> entrySet() {
				return new AbstractSet<Map.Entry<String,String>>() {

					@Override
					public Iterator<Entry<String, String>> iterator() {
						Iterator<Entry<String, List<String>>> delegate = QDCSS.this.entrySet().iterator();
						return new Iterator<Map.Entry<String,String>>() {

							@Override
							public boolean hasNext() {
								return delegate.hasNext();
							}

							@Override
							public Entry<String, String> next() {
								Entry<String, List<String>> den = delegate.next();
								return new SimpleImmutableEntry<>(den.getKey(), getLast(den.getValue()));
							}
						};
					}

					@Override
					public int size() {
						return size();
					}

				};
			}

		};
	}
	
	private static final Pattern JUNK_PATTERN = Pattern.compile("^(\\s*(/\\*.*?\\*/)?\\s*)*$", Pattern.DOTALL);
	private static final Pattern RULESET_PATTERN = Pattern.compile("[#.]?(\\w+?)\\s*\\{(.*?)\\}", Pattern.DOTALL);
	private static final Pattern RULE_PATTERN = Pattern.compile("(\\S+?)\\s*:\\s*(\\\".*?\\\"|'.*?'|\\S+?)\\s*(;|$)");
	
	public static QDCSS load(String fileName, String s) throws SyntaxErrorException {
		// vanilla CSS is a very simple grammar, so we can parse it using only regexes
		Map<String, List<BlameString>> data = new LinkedHashMap<>();
		Matcher ruleset = RULESET_PATTERN.matcher(s);
		int lastEnd = 0;
		while (ruleset.find()) {
			String skipped = s.substring(lastEnd, ruleset.start());
			if (!JUNK_PATTERN.matcher(skipped).matches()) {
				throw new SyntaxErrorException("Expected a ruleset near line "+getLine(s, ruleset.start())+" in "+fileName);
			}
			String selector = ruleset.group(1);
			String rules = ruleset.group(2);
			Matcher rule = RULE_PATTERN.matcher(rules);
			int lastRulesEnd = 0;
			while (rule.find()) {
				String skippedRule = rules.substring(lastRulesEnd, rule.start());
				if (!JUNK_PATTERN.matcher(skippedRule).matches()) {
					throw new SyntaxErrorException("Expected a rule near line "+getLine(s, ruleset.start(2)+rule.start())+" in "+fileName);
				}
				String property = rule.group(1);
				String value = rule.group(2);
				String key = selector+"."+property;
				if (!data.containsKey(key)) {
					data.put(key, Lists.newArrayList());
				}
				data.get(key).add(new BlameString(value, fileName, getLine(s, ruleset.start(2)+rule.start())));
				lastRulesEnd = rule.end();
			}
			String skippedRule = rules.substring(lastRulesEnd);
			if (!JUNK_PATTERN.matcher(skippedRule).matches()) {
				throw new SyntaxErrorException("Expected a rule near line "+getLine(s, ruleset.start(2)+lastRulesEnd)+" in "+fileName);
			}
			lastEnd = ruleset.end();
		}
		String skipped = s.substring(lastEnd);
		if (!JUNK_PATTERN.matcher(skipped).matches()) {
			throw new SyntaxErrorException("Expected a ruleset or EOF near line "+getLine(s, lastEnd)+" in "+fileName);
		}
		return new QDCSS(fileName, data);
	}

	private static int getLine(String s, int start) {
		int line = 1;
		for (int i = 0; i < start; i++) {
			if (s.charAt(i) == '\n') {
				line++;
			}
		}
		return line;
	}

	public static QDCSS load(File f) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			return load(f.getName(), in);
		}
	}

	public static QDCSS load(Path p) throws IOException {
		try (InputStream in = Files.newInputStream(p)) {
			return load(p.getFileName().toString(), in);
		}
	}

	private static final Splitter SLASH_SPLITTER = Splitter.on('/');
	
	public static QDCSS load(URL u) throws IOException {
		try (InputStream in = u.openStream()) {
			return load(Iterables.getLast(SLASH_SPLITTER.split(u.getPath())), in);
		}
	}

	public static QDCSS load(String fileName, InputStream in) throws IOException {
		return load(fileName, new InputStreamReader(in, StandardCharsets.UTF_8));
	}

	public static QDCSS load(String fileName, Reader r) throws IOException {
		return load(fileName, CharStreams.toString(r));
	}

}
