package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.robotframework.ide.core.testData.text.lexer.GroupedSameTokenType;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;

import com.google.common.collect.LinkedListMultimap;


/**
 * Common assertions for {@link TokenOutput} object contents.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TokenOutputAsserationHelper {

    public static void assertPositionMarkers(TokenOutput output) {
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<RobotToken> tokens = output.getTokens();
        Set<RobotType> typeSetFound = new HashSet<>();
        for (int i = 0; i < tokens.size(); i++) {
            RobotToken cToken = tokens.get(i);
            assertThat(tokensPosition.get(cToken.getType())).contains(i);
            typeSetFound.add(cToken.getType());
        }

        Set<RobotType> typeSet = tokensPosition.keySet();
        assertThat(typeSet).hasSameSizeAs(typeSetFound);
        assertThat(typeSet).hasSameElementsAs(typeSetFound);
    }


    public static void assertTokensForUnknownWords(TokenOutput out,
            RobotType[] types, int startTokenPos, int startLine,
            String[] textForCorrespondingUnknownWords) {
        List<RobotToken> tokens = out.getTokens();
        int typesLength = types.length;
        CircullarArrayIterator<RobotType> iter = new CircullarArrayIterator<>(
                types);

        assertThat(tokens).isNotNull();
        assertThat(tokens).isNotEmpty();
        assertThat((tokens.size() - startTokenPos) % typesLength).isEqualTo(0);
        int correspondingTextIndex = 0;
        int line = startLine;
        int column = LinearPositionMarker.THE_FIRST_COLUMN;
        for (int tokId = startTokenPos; tokId < tokens.size(); tokId++) {
            RobotToken robotToken = tokens.get(tokId);
            assertThat(robotToken).isNotNull();
            RobotType type = iter.next();
            assertThat(robotToken.getType()).isEqualTo(type);

            if (type == RobotTokenType.END_OF_LINE) {
                assertThat(robotToken.getText()).isNull();
                assertStartPosition(robotToken, line, column);
                assertEndPosition(robotToken, line, column);
                line++;
                column = LinearPositionMarker.THE_FIRST_COLUMN;
            } else {
                if ((robotToken.getType() == RobotTokenType.UNKNOWN || robotToken
                        .getType() == RobotWordType.UNKNOWN_WORD)
                        && correspondingTextIndex < textForCorrespondingUnknownWords.length) {
                    assertThat(robotToken.getText().toString())
                            .isEqualTo(
                                    textForCorrespondingUnknownWords[correspondingTextIndex]);
                    correspondingTextIndex++;
                } else if (robotToken.getType().getClass() != GroupedSameTokenType.class) {
                    assertThat(robotToken.getText().toString()).isEqualTo(
                            type.toWrite());
                } else if (robotToken.getType().getClass() == GroupedSameTokenType.class) {
                    assertThat(robotToken.getText().toString()).matches(
                            "["
                                    + ((GroupedSameTokenType) robotToken
                                            .getType()).getWrappedType()
                                            .toWrite() + "]+");
                }
                assertStartPosition(robotToken, line, column);
                if (robotToken.getText() != null) {
                    column += robotToken.getText().length();
                } else {
                    column++;
                }
                assertEndPosition(robotToken, line, column);
            }
        }
    }


    public static void assertTokens(TokenOutput out, RobotType[] types,
            int startTokenPos, int startLine) {
        List<RobotToken> tokens = out.getTokens();
        int typesLength = types.length;
        CircullarArrayIterator<RobotType> iter = new CircullarArrayIterator<>(
                types);

        assertThat(tokens).isNotNull();
        assertThat(tokens).isNotEmpty();
        assertThat((tokens.size() - startTokenPos) % typesLength).isEqualTo(0);
        int line = startLine;
        int column = LinearPositionMarker.THE_FIRST_COLUMN;
        for (int tokId = startTokenPos; tokId < tokens.size(); tokId++) {
            RobotToken robotToken = tokens.get(tokId);
            assertThat(robotToken).isNotNull();
            RobotType type = iter.next();
            assertThat(robotToken.getType()).isEqualTo(type);

            if (type == RobotTokenType.END_OF_LINE) {
                assertThat(robotToken.getText()).isNull();
                assertStartPosition(robotToken, line, column);
                assertEndPosition(robotToken, line, column);
                line++;
                column = LinearPositionMarker.THE_FIRST_COLUMN;
            } else {
                if (robotToken.getType().getClass() != GroupedSameTokenType.class) {
                    assertThat(robotToken.getText().toString()).isEqualTo(
                            type.toWrite());
                } else {
                    assertThat(robotToken.getText().toString()).matches(
                            "["
                                    + ((GroupedSameTokenType) robotToken
                                            .getType()).getWrappedType()
                                            .toWrite() + "]+");
                }
                assertStartPosition(robotToken, line, column);
                if (robotToken.getText() != null) {
                    column += robotToken.getText().length();
                } else {
                    column++;
                }
                assertEndPosition(robotToken, line, column);
            }
        }
    }


    public static void assertCurrentPosition(TokenOutput output) {
        LinearPositionMarker currentMarker = output.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        LinkedListMultimap<RobotType, Integer> tokensPosition = output
                .getTokensPosition();
        assertThat(tokensPosition).isNotNull();
        List<Integer> crTokens = tokensPosition
                .get(RobotTokenType.CARRIAGE_RETURN);
        List<Integer> lfTokens = tokensPosition.get(RobotTokenType.LINE_FEED);
        int line = Math.max(crTokens.size(), lfTokens.size());
        assertThat(currentMarker.getLine()).isEqualTo(line + 1);
        assertThat(currentMarker.getColumn()).isEqualTo(currentColumn(output));
    }


    private static int currentColumn(TokenOutput output) {
        List<RobotToken> tokens = output.getTokens();
        int column = LinearPositionMarker.THE_FIRST_COLUMN;
        if (!tokens.isEmpty()) {
            RobotToken robotToken = tokens.get(tokens.size() - 1);
            column = robotToken.getEndPosition().getColumn();
            if (robotToken.getType() == RobotTokenType.END_OF_LINE) {
                column = LinearPositionMarker.THE_FIRST_COLUMN;
            }
        }

        return column;
    }


    public static void assertStartPosition(RobotToken token, int line,
            int column) {
        LinearPositionMarker startPosition = token.getStartPosition();
        assertThat(startPosition).isNotNull();
        assertThat(startPosition.getLine()).isEqualTo(line);
        assertThat(startPosition.getColumn()).isEqualTo(column);
    }


    public static void assertEndPosition(RobotToken token, int line, int column) {
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(line);
        assertThat(endPosition.getColumn()).isEqualTo(column);
    }


    public static void assertIsTotalEmpty(final TokenOutput output) {
        assertThat(output.getTokens()).isEmpty();
        assertThat(output.getTokensPosition().asMap()).isEmpty();
        assertCurrentMarkerPosition(output,
                LinearPositionMarker.THE_FIRST_LINE,
                LinearPositionMarker.THE_FIRST_COLUMN);
    }


    public static void assertCurrentMarkerPosition(final TokenOutput out,
            int line, int column) {
        LinearPositionMarker currentMarker = out.getCurrentMarker();
        assertThat(currentMarker).isNotNull();
        assertThat(currentMarker.getLine()).isEqualTo(line);
        assertThat(currentMarker.getColumn()).isEqualTo(column);
    }


    public static TokenOutput createTokenOutputWithTwoTabulatorsInside() {
        TokenOutput output = new TokenOutput();
        LinearPositionMarker beginMarker = output.getCurrentMarker();

        RobotToken tabulatorTokenOne = new RobotToken(beginMarker,
                new StringBuilder("\t"));
        tabulatorTokenOne.setType(RobotTokenType.SINGLE_TABULATOR);
        output.getTokens().add(tabulatorTokenOne);
        output.getTokensPosition().put(RobotTokenType.SINGLE_TABULATOR, 0);

        RobotToken tabulatorTokenTwo = new RobotToken(
                tabulatorTokenOne.getEndPosition(), new StringBuilder("\t"));
        tabulatorTokenTwo.setType(RobotTokenType.SINGLE_TABULATOR);
        output.getTokens().add(tabulatorTokenTwo);
        output.getTokensPosition().put(RobotTokenType.SINGLE_TABULATOR, 1);

        output.setCurrentMarker(tabulatorTokenTwo.getEndPosition());

        return output;
    }


    public static TokenOutput createTokenOutputWithTwoAsterisksInside() {
        TokenOutput output = new TokenOutput();
        LinearPositionMarker beginMarker = output.getCurrentMarker();

        RobotToken asteriskTokenOne = new RobotToken(beginMarker,
                new StringBuilder("*"));
        asteriskTokenOne.setType(RobotTokenType.SINGLE_ASTERISK);
        output.getTokens().add(asteriskTokenOne);
        output.getTokensPosition().put(RobotTokenType.SINGLE_ASTERISK, 0);

        RobotToken asteriskTokenTwo = new RobotToken(
                asteriskTokenOne.getEndPosition(), new StringBuilder("*"));
        asteriskTokenTwo.setType(RobotTokenType.SINGLE_ASTERISK);
        output.getTokens().add(asteriskTokenTwo);
        output.getTokensPosition().put(RobotTokenType.SINGLE_ASTERISK, 1);

        output.setCurrentMarker(asteriskTokenTwo.getEndPosition());

        return output;
    }
}
