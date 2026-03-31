import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LexicalAnalyzer {
    // Java reserved words that should stay as their own token names in the output
    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "break", "case", "catch", "class", "const", "continue",
            "default", "do", "else", "enum", "extends", "final", "finally", "for", "goto",
            "if", "implements", "import", "instanceof", "interface", "main", "native", "new",
            "package", "private", "protected", "public", "return", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try",
            "void", "volatile", "while"
    );

    // Primitive types and common Java wrapper/reference types
    private static final Set<String> TYPES = new HashSet<>(Set.of(
            "byte", "short", "int", "long", "float", "double", "char", "boolean", "String",
            "Boolean", "Integer", "Double", "Float", "Character", "Long", "Short", "Byte"
    ));

    // Check multi-character operators
    private static final Set<String> TWO_CHAR_OPERATORS = Set.of(
            "==", "!=", ">=", "<=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=", "%="
    );

    // Check single-character operators
    private static final Set<Character> ONE_CHAR_OPERATORS = Set.of(
            '+', '-', '*', '/', '%', '=', '>', '<', '!', '&', '|'
    );

    // Check symbols
    private static final Set<Character> SYMBOLS = Set.of(
            '{', '}', '[', ']', '(', ')', '.', ';', ',', ':'
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LexicalAnalyzer <input-file>");
            return;
        }

        try {
            String source = Files.readString(Path.of(args[0]));
            List<String> tokens = tokenize(source);
            System.out.println(String.join(" ", tokens));
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }

    private static List<String> tokenize(String source) {
        List<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < source.length()) {
            char ch = source.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // Skip single-line comments
            if (startsWith(source, i, "//")) {
                i += 2;
                while (i < source.length() && source.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            // Skip block comments
            if (startsWith(source, i, "/*")) {
                i += 2;
                while (i + 1 < source.length() && !startsWith(source, i, "*/")) {
                    i++;
                }
                i = Math.min(i + 2, source.length());
                continue;
            }

            // Treat both string and character constants as literals
            if (ch == '"') {
                i++;
                while (i < source.length()) {
                    char current = source.charAt(i);
                    if (current == '\\' && i + 1 < source.length()) {
                        i += 2;
                        continue;
                    }
                    if (current == '"') {
                        i++;
                        break;
                    }
                    i++;
                }
                tokens.add("literal");
                continue;
            }

            if (ch == '\'') {
                i++;
                while (i < source.length()) {
                    char current = source.charAt(i);
                    if (current == '\\' && i + 1 < source.length()) {
                        i += 2;
                        continue;
                    }
                    if (current == '\'') {
                        i++;
                        break;
                    }
                    i++;
                }
                tokens.add("literal");
                continue;
            }

            // Read integers, decimals, and simple numeric suffixes
            if (Character.isDigit(ch)) {
                boolean hasDot = false;
                while (i < source.length()) {
                    char current = source.charAt(i);
                    if (Character.isDigit(current)) {
                        i++;
                    } else if (current == '.' && !hasDot && i + 1 < source.length()
                            && Character.isDigit(source.charAt(i + 1))) {
                        hasDot = true;
                        i++;
                    } else {
                        break;
                    }
                }

                if (i < source.length() && "fFdDlL".indexOf(source.charAt(i)) >= 0) {
                    if (source.charAt(i) == 'd' || source.charAt(i) == 'D' || source.charAt(i) == 'f'
                            || source.charAt(i) == 'F') {
                        hasDot = true;
                    }
                    i++;
                }

                tokens.add(hasDot ? "doubleNum" : "intNum");
                continue;
            }

            // Read identifiers, keywords, types, and simple class names
            if (Character.isLetter(ch) || ch == '_') {
                int start = i;
                i++;
                while (i < source.length()) {
                    char current = source.charAt(i);
                    if (Character.isLetterOrDigit(current) || current == '_') {
                        i++;
                    } else {
                        break;
                    }
                }

                String word = source.substring(start, i);
                tokens.add(classifyWord(word));
                continue;
            }

            if (i + 1 < source.length()) {
                String twoChar = source.substring(i, i + 2);
                if (TWO_CHAR_OPERATORS.contains(twoChar)) {
                    tokens.add(twoChar);
                    i += 2;
                    continue;
                }
            }

            if (ONE_CHAR_OPERATORS.contains(ch) || SYMBOLS.contains(ch)) {
                tokens.add(String.valueOf(ch));
                i++;
                continue;
            }

            i++;
        }

        return tokens;
    }

    private static String classifyWord(String word) {
        if (KEYWORDS.contains(word)) {
            return word;
        }
        if (TYPES.contains(word)) {
            return "type";
        }
        if (!word.isEmpty() && Character.isUpperCase(word.charAt(0))) {
            return "Class";
        }
        return "var";
    }

    private static boolean startsWith(String text, int index, String prefix) {
        return text.regionMatches(index, prefix, 0, prefix.length());
    }
}
