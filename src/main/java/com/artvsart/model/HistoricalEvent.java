package com.artvsart.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public enum HistoricalEvent {
    DOMESDAY_BOOK_COMPLETED(
            1086, EventFamiliarity.RECOGNIZABLE,
            "the completion of the Domesday Book",
            "Domesday Book"
    ),
    FIRST_CRUSADE_BEGAN(
            1096, EventFamiliarity.FAMILIAR,
            "the beginning of the First Crusade",
            "First Crusade"
    ),
    BATTLE_OF_TINCHEBRAY(
            1106, EventFamiliarity.OBSCURE,
            "the Battle of Tinchebray",
            "Battle of Tinchebray"
    ),
    KNIGHTS_TEMPLAR_FOUNDED(
            1119, EventFamiliarity.RECOGNIZABLE,
            "the founding of the Knights Templar",
            "Knights Templar"
    ),
    CONCORDAT_OF_WORMS(
            1122, EventFamiliarity.OBSCURE,
            "the signing of the Concordat of Worms",
            "Concordat of Worms"
    ),
    LOUIS_VII_BECAME_KING(
            1137, EventFamiliarity.OBSCURE,
            "Louis VII's accession to the French throne",
            "Louis VII of France"
    ),
    SECOND_CRUSADE_BEGAN(
            1147, EventFamiliarity.OBSCURE,
            "the beginning of the Second Crusade",
            "Second Crusade"
    ),
    FREDERICK_BARBAROSSA_ELECTED(
            1152, EventFamiliarity.OBSCURE,
            "Frederick Barbarossa's election as king of Germany",
            "Frederick Barbarossa"
    ),
    NOTRE_DAME_CONSTRUCTION_BEGAN(
            1163, EventFamiliarity.RECOGNIZABLE,
            "the beginning of construction on Notre-Dame de Paris",
            "Notre-Dame de Paris"
    ),
    THOMAS_BECKET_MURDERED(
            1170, EventFamiliarity.RECOGNIZABLE,
            "the murder of Thomas Becket",
            "Thomas Becket"
    ),
    ERFURT_LATRINE_DISASTER(
            1184, EventFamiliarity.OBSCURE,
            "the Erfurt latrine disaster",
            "Erfurt latrine disaster",
            "A floor collapsed during a gathering of nobles, sending attendees into a cesspit below. Many died."
    ),
    SALADIN_CAPTURED_JERUSALEM(
            1187, EventFamiliarity.RECOGNIZABLE,
            "Saladin's capture of Jerusalem",
            "Siege of Jerusalem (1187)"
    ),
    THIRD_CRUSADE_ENDED(
            1192, EventFamiliarity.RECOGNIZABLE,
            "the end of the Third Crusade",
            "Third Crusade"
    ),
    SACK_OF_CONSTANTINOPLE(
            1204, EventFamiliarity.RECOGNIZABLE,
            "the Fourth Crusade's sack of Constantinople",
            "Sack of Constantinople"
    ),
    MAGNA_CARTA(
            1215, EventFamiliarity.FAMILIAR,
            "the sealing of Magna Carta",
            "Magna Carta"
    ),
    AMIENS_CATHEDRAL_CONSTRUCTION_BEGAN(
            1220, EventFamiliarity.OBSCURE,
            "the beginning of construction on Amiens Cathedral",
            "Amiens Cathedral"
    ),
    CORDOBA_CAPTURED(
            1236, EventFamiliarity.OBSCURE,
            "Ferdinand III's capture of Cordoba",
            "Siege of Córdoba (1236)"
    ),
    BATTLE_OF_MOHI(
            1241, EventFamiliarity.OBSCURE,
            "the Battle of Mohi",
            "Battle of Mohi"
    ),
    HENRYS_ELEPHANT_ARRIVED(
            1255, EventFamiliarity.OBSCURE,
            "the arrival of Henry III's elephant in England",
            "Elephant of Henry III",
            "A diplomatic gift needed unusually large accommodation: a special elephant house at the Tower of London."
    ),
    MONGOLS_SACKED_BAGHDAD(
            1258, EventFamiliarity.RECOGNIZABLE,
            "the Mongol sack of Baghdad",
            "Siege of Baghdad (1258)"
    ),
    BATTLE_OF_AIN_JALUT(
            1260, EventFamiliarity.OBSCURE,
            "the Battle of Ain Jalut",
            "Battle of Ain Jalut"
    ),
    MARCO_POLO_JOURNEY_BEGAN(
            1271, EventFamiliarity.FAMILIAR,
            "the beginning of Marco Polo's journey to Asia",
            "Marco Polo"
    ),
    SICILIAN_VESPERS(
            1282, EventFamiliarity.OBSCURE,
            "the Sicilian Vespers uprising",
            "Sicilian Vespers"
    ),
    FALL_OF_ACRE(
            1291, EventFamiliarity.OBSCURE,
            "the fall of Acre",
            "Siege of Acre (1291)"
    ),
    KNIGHTS_TEMPLAR_ARRESTED(
            1307, EventFamiliarity.OBSCURE,
            "the arrest of the Knights Templar in France",
            "Trials of the Knights Templar"
    ),
    GREAT_FAMINE_OF_EUROPE(
            1315, EventFamiliarity.OBSCURE,
            "the beginning of the Great Famine in Europe",
            "Great Famine of 1315–1317"
    ),
    MANSA_MUSA_PILGRIMAGE(
            1324, EventFamiliarity.RECOGNIZABLE,
            "Mansa Musa's pilgrimage to Mecca",
            "Mansa Musa"
    ),
    HUNDRED_YEARS_WAR_BEGAN(
            1337, EventFamiliarity.FAMILIAR,
            "the beginning of the Hundred Years' War",
            "Hundred Years' War"
    ),
    BLACK_DEATH_IN_EUROPE(
            1347, EventFamiliarity.FAMILIAR,
            "the arrival of the Black Death in Europe",
            "Black Death"
    ),
    GOLDEN_BULL_ISSUED(
            1356, EventFamiliarity.OBSCURE,
            "the issuance of the Golden Bull",
            "Golden Bull of 1356"
    ),
    MING_DYNASTY_ESTABLISHED(
            1368, EventFamiliarity.RECOGNIZABLE,
            "the establishment of the Ming dynasty",
            "Ming dynasty"
    ),
    WESTERN_SCHISM_BEGAN(
            1378, EventFamiliarity.OBSCURE,
            "the beginning of the Western Schism",
            "Western Schism"
    ),
    PEASANTS_REVOLT(
            1381, EventFamiliarity.RECOGNIZABLE,
            "the Peasants' Revolt in England",
            "Peasants' Revolt"
    ),
    JOSEON_DYNASTY_FOUNDED(
            1392, EventFamiliarity.OBSCURE,
            "the founding of the Joseon dynasty",
            "Joseon"
    ),
    BAL_DES_ARDENTS(
            1393, EventFamiliarity.OBSCURE,
            "the Bal des Ardents",
            "Bal des Ardents",
            "A royal masquerade turned deadly when dancers' costumes caught fire. Charles VI survived; four dancers did not."
    ),
    ZHENG_HE_FIRST_VOYAGE(
            1405, EventFamiliarity.RECOGNIZABLE,
            "the beginning of Zheng He's first voyage",
            "Ming treasure voyages"
    ),
    MING_GIRAFFE_ARRIVED(
            1414, EventFamiliarity.OBSCURE,
            "the arrival of a giraffe at the Ming court",
            "Qilin",
            "Bengali envoys presented a giraffe to the Yongle emperor. Courtiers celebrated it as a qilin, an auspicious mythical creature."
    ),
    BATTLE_OF_AGINCOURT(
            1415, EventFamiliarity.RECOGNIZABLE,
            "the Battle of Agincourt",
            "Battle of Agincourt"
    ),
    SIEGE_OF_ORLEANS_LIFTED(
            1429, EventFamiliarity.RECOGNIZABLE,
            "Joan of Arc's lifting of the siege of Orleans",
            "Siege of Orléans"
    ),
    MEDICI_RETURNED_TO_FLORENCE(
            1434, EventFamiliarity.RECOGNIZABLE,
            "Cosimo de' Medici's return to power in Florence",
            "Cosimo de' Medici"
    ),
    BATTLE_OF_VARNA(
            1444, EventFamiliarity.OBSCURE,
            "the Battle of Varna",
            "Battle of Varna"
    ),
    FALL_OF_CONSTANTINOPLE(
            1453, EventFamiliarity.FAMILIAR,
            "the fall of Constantinople",
            "Fall of Constantinople"
    ),
    FERDINAND_AND_ISABELLA_MARRIED(
            1469, EventFamiliarity.OBSCURE,
            "the marriage of Ferdinand of Aragon and Isabella of Castile",
            "Ferdinand II of Aragon"
    ),
    SPANISH_INQUISITION_ESTABLISHED(
            1478, EventFamiliarity.RECOGNIZABLE,
            "the establishment of the Spanish Inquisition",
            "Spanish Inquisition"
    ),
    DIAS_ROUNDED_CAPE_OF_GOOD_HOPE(
            1488, EventFamiliarity.RECOGNIZABLE,
            "Bartolomeu Dias's rounding of the Cape of Good Hope",
            "Bartolomeu Dias"
    ),
    COLUMBUS_REACHED_AMERICAS(
            1492, EventFamiliarity.FAMILIAR,
            "Columbus's arrival in the Americas",
            "Voyages of Christopher Columbus"
    ),
    SAFAVID_DYNASTY_ESTABLISHED(
            1501, EventFamiliarity.OBSCURE,
            "the establishment of the Safavid dynasty",
            "Safavid Iran"
    ),
    DURERS_RHINOCEROS(
            1515, EventFamiliarity.OBSCURE,
            "the creation of Durer's rhinoceros woodcut",
            "Dürer's Rhinoceros",
            "Durer made his famous rhinoceros without seeing the animal, working from a description and sketch. The result came with imaginary armor."
    ),
    PROTESTANT_REFORMATION(
            1517, EventFamiliarity.FAMILIAR,
            "the beginning of the Protestant Reformation",
            "Reformation"
    ),
    STRASBOURG_DANCING_PLAGUE(
            1518, EventFamiliarity.RECOGNIZABLE,
            "the Strasbourg dancing plague",
            "Dancing plague of 1518",
            "An outbreak of prolonged dancing baffled Strasbourg. Its cause, scale and reported death toll remain disputed."
    ),
    AZTEC_EMPIRE_FELL(
            1521, EventFamiliarity.FAMILIAR,
            "the fall of the Aztec Empire",
            "Fall of Tenochtitlan"
    ),
    ACT_OF_SUPREMACY(
            1534, EventFamiliarity.OBSCURE,
            "the passage of the English Act of Supremacy",
            "Acts of Supremacy"
    ),
    COPERNICUS_PUBLISHED_HELIOCENTRIC_MODEL(
            1543, EventFamiliarity.RECOGNIZABLE,
            "Copernicus's publication of his heliocentric model",
            "De revolutionibus orbium coelestium"
    ),
    PEACE_OF_AUGSBURG(
            1555, EventFamiliarity.OBSCURE,
            "the signing of the Peace of Augsburg",
            "Peace of Augsburg"
    ),
    GREAT_SIEGE_OF_MALTA(
            1565, EventFamiliarity.OBSCURE,
            "the Great Siege of Malta",
            "Great Siege of Malta"
    ),
    BATTLE_OF_LEPANTO(
            1571, EventFamiliarity.OBSCURE,
            "the Battle of Lepanto",
            "Battle of Lepanto"
    ),
    SPANISH_ARMADA_DEFEATED(
            1588, EventFamiliarity.FAMILIAR,
            "the defeat of the Spanish Armada",
            "Spanish Armada"
    ),
    EDICT_OF_NANTES(
            1598, EventFamiliarity.OBSCURE,
            "the issuance of the Edict of Nantes",
            "Edict of Nantes"
    ),
    JAMESTOWN_FOUNDED(
            1607, EventFamiliarity.RECOGNIZABLE,
            "the founding of Jamestown",
            "Jamestown, Virginia"
    ),
    THIRTY_YEARS_WAR_BEGAN(
            1618, EventFamiliarity.RECOGNIZABLE,
            "the beginning of the Thirty Years' War",
            "Thirty Years' War"
    ),
    MAYFLOWER_REACHED_PLYMOUTH(
            1620, EventFamiliarity.FAMILIAR,
            "the Mayflower's arrival at Plymouth",
            "Mayflower"
    ),
    VASA_SANK(
            1628, EventFamiliarity.RECOGNIZABLE,
            "the sinking of the Vasa on its maiden voyage",
            "Vasa (ship)",
            "The Swedish warship sank just over a kilometer into its maiden voyage in Stockholm. Its impressive appearance concealed fatal instability."
    ),
    GALILEO_TRIED_BY_INQUISITION(
            1633, EventFamiliarity.RECOGNIZABLE,
            "Galileo's trial by the Roman Inquisition",
            "Galileo affair"
    ),
    TULIP_MARKET_COLLAPSED(
            1637, EventFamiliarity.RECOGNIZABLE,
            "the collapse of the Dutch tulip market",
            "Tulip mania",
            "Contracts for fashionable tulip bulbs soared, then collapsed. Later tales of nationwide ruin exaggerated the damage."
    ),
    PEACE_OF_WESTPHALIA(
            1648, EventFamiliarity.OBSCURE,
            "the signing of the Peace of Westphalia",
            "Peace of Westphalia"
    ),
    LEVIATHAN_PUBLISHED(
            1651, EventFamiliarity.OBSCURE,
            "the publication of Hobbes's Leviathan",
            "Leviathan (Hobbes book)"
    ),
    GREAT_FIRE_OF_LONDON(
            1666, EventFamiliarity.FAMILIAR,
            "the Great Fire of London",
            "Great Fire of London"
    ),
    FRANCO_DUTCH_WAR_BEGAN(
            1672, EventFamiliarity.OBSCURE,
            "the beginning of the Franco-Dutch War",
            "Franco-Dutch War"
    ),
    COFFEE_PETITION_PUBLISHED(
            1674, EventFamiliarity.OBSCURE,
            "the publication of the Women's Petition Against Coffee",
            "History of coffee",
            "An anonymous pamphlet blamed coffee for men's neglect of home life. Its title is not proof that it represented women's views."
    ),
    PRINCIPIA_PUBLISHED(
            1687, EventFamiliarity.RECOGNIZABLE,
            "the publication of Newton's Principia",
            "Philosophiæ Naturalis Principia Mathematica"
    ),
    SALEM_WITCH_TRIALS(
            1692, EventFamiliarity.FAMILIAR,
            "the Salem witch trials",
            "Salem witch trials"
    ),
    ACTS_OF_UNION(
            1707, EventFamiliarity.RECOGNIZABLE,
            "the Acts of Union that created Great Britain",
            "Acts of Union 1707"
    ),
    JACOBITE_RISING(
            1715, EventFamiliarity.OBSCURE,
            "the Jacobite rising of 1715",
            "Jacobite rising of 1715"
    ),
    TREATY_OF_NYSTAD(
            1721, EventFamiliarity.OBSCURE,
            "the Treaty of Nystad ending the Great Northern War",
            "Treaty of Nystad"
    ),
    MARY_TOFT_HOAX(
            1726, EventFamiliarity.OBSCURE,
            "Mary Toft's rabbit birth hoax",
            "Mary Toft",
            "Doctors were fooled by claims that a woman had given birth to rabbits. The exposed hoax made medical confidence a target of satire."
    ),
    FLYING_SHUTTLE_PATENTED(
            1733, EventFamiliarity.OBSCURE,
            "the patenting of the flying shuttle",
            "Flying shuttle"
    ),
    WAR_OF_AUSTRIAN_SUCCESSION_BEGAN(
            1740, EventFamiliarity.OBSCURE,
            "the beginning of the War of the Austrian Succession",
            "War of the Austrian Succession"
    ),
    SEVEN_YEARS_WAR_BEGAN(
            1756, EventFamiliarity.RECOGNIZABLE,
            "the beginning of the Seven Years' War",
            "Seven Years' War"
    ),
    STAMP_ACT(
            1765, EventFamiliarity.RECOGNIZABLE,
            "the passage of the Stamp Act",
            "Stamp Act 1765"
    ),
    VENUS_OBSERVED_FROM_TAHITI(
            1769, EventFamiliarity.OBSCURE,
            "the observation of Venus crossing the Sun from Tahiti",
            "1769 transit of Venus observed from Tahiti",
            "Cook's expedition crossed an ocean to watch a tiny dot cross the Sun, helping an international effort to measure the solar system."
    ),
    DECLARATION_OF_INDEPENDENCE(
            1776, EventFamiliarity.FAMILIAR,
            "the adoption of the American Declaration of Independence",
            "United States Declaration of Independence"
    ),
    BALLOON_ANIMAL_PASSENGERS(
            1783, EventFamiliarity.RECOGNIZABLE,
            "the Montgolfiers' balloon flight with animal passengers",
            "Montgolfier brothers",
            "A sheep, a duck and a rooster took a balloon ride at Versailles before humans made their own untethered flights."
    ),
    FRENCH_REVOLUTION(
            1789, EventFamiliarity.FAMILIAR,
            "the beginning of the French Revolution",
            "French Revolution"
    ),
    HAITIAN_REVOLUTION_BEGAN(
            1791, EventFamiliarity.RECOGNIZABLE,
            "the beginning of the Haitian Revolution",
            "Haitian Revolution"
    ),
    ROSETTA_STONE_FOUND(
            1799, EventFamiliarity.RECOGNIZABLE,
            "the rediscovery of the Rosetta Stone",
            "Rosetta Stone",
            "Soldiers working on a fort in Egypt found an inscribed stone that became a key to deciphering Egyptian hieroglyphs."
    ),
    HAITI_DECLARED_INDEPENDENCE(
            1804, EventFamiliarity.RECOGNIZABLE,
            "Haiti's declaration of independence",
            "Haitian Declaration of Independence"
    ),
    LONDON_BEER_FLOOD(
            1814, EventFamiliarity.OBSCURE,
            "the London Beer Flood",
            "London Beer Flood",
            "A brewery vat burst and released a destructive wave of beer into nearby homes. Eight people died."
    ),
    BATTLE_OF_WATERLOO(
            1815, EventFamiliarity.FAMILIAR,
            "the Battle of Waterloo",
            "Battle of Waterloo"
    ),
    GREEK_WAR_OF_INDEPENDENCE_BEGAN(
            1821, EventFamiliarity.OBSCURE,
            "the beginning of the Greek War of Independence",
            "Greek War of Independence"
    ),
    GIRAFFE_REACHED_PARIS(
            1827, EventFamiliarity.OBSCURE,
            "the arrival of a giraffe in Paris",
            "Zarafa (giraffe)",
            "A giraffe sent by Muhammad Ali of Egypt walked from Marseille to Paris and inspired a fashion craze, including giraffe-themed hairstyles."
    ),
    JULY_REVOLUTION(
            1830, EventFamiliarity.OBSCURE,
            "the July Revolution in France",
            "July Revolution"
    ),
    GREAT_MOON_HOAX(
            1835, EventFamiliarity.RECOGNIZABLE,
            "the Great Moon Hoax",
            "Great Moon Hoax",
            "A New York newspaper reported imaginary lunar creatures and civilization as astronomical discoveries. The Moon had acquired a fictional population."
    ),
    REVOLUTIONS_OF_1848(
            1848, EventFamiliarity.RECOGNIZABLE,
            "the Revolutions of 1848",
            "Revolutions of 1848"
    ),
    GREAT_STINK(
            1858, EventFamiliarity.RECOGNIZABLE,
            "London's Great Stink",
            "Great Stink",
            "The polluted Thames smelled so strongly that it disrupted Parliament. Sewage became a political issue impossible to ignore."
    ),
    ORIGIN_OF_SPECIES_PUBLISHED(
            1859, EventFamiliarity.FAMILIAR,
            "the publication of Darwin's On the Origin of Species",
            "On the Origin of Species"
    ),
    AMERICAN_CIVIL_WAR_BEGAN(
            1861, EventFamiliarity.FAMILIAR,
            "the beginning of the American Civil War",
            "American Civil War"
    ),
    FIRST_IMPRESSIONIST_EXHIBITION(
            1874, EventFamiliarity.RECOGNIZABLE,
            "the opening of the first Impressionist exhibition",
            "First Impressionist Exhibition",
            "The artists exhibited in a former photography studio. A mocking review helped give Impressionism its name."
    ),
    EIFFEL_TOWER_OPENED(
            1889, EventFamiliarity.FAMILIAR,
            "the opening of the Eiffel Tower",
            "Eiffel Tower",
            "Artists had protested the tower before it opened. The much-criticized structure became a hit with visitors and, eventually, a symbol of Paris."
    ),
    NEW_ZEALAND_WOMEN_VOTED(
            1893, EventFamiliarity.RECOGNIZABLE,
            "women's first parliamentary vote in New Zealand",
            "Women's suffrage in New Zealand",
            "After a long suffrage campaign, women in New Zealand gained the parliamentary vote and used it in that year's election."
    ),
    FIRST_MODERN_OLYMPICS(
            1896, EventFamiliarity.FAMILIAR,
            "the first modern Olympic Games",
            "1896 Summer Olympics"
    ),
    WRIGHT_FLYER_FIRST_FLIGHT(
            1903, EventFamiliarity.FAMILIAR,
            "the Wright Flyer's first flight",
            "Wright Flyer",
            "Orville's first flight lasted twelve seconds and covered 120 feet. A very short trip opened a very large chapter in aviation."
    ),
    PEKING_PARIS_RACE(
            1907, EventFamiliarity.OBSCURE,
            "the first Peking to Paris motor race",
            "Peking to Paris",
            "Early motorists crossed Asia and Europe in a newspaper-inspired challenge. Fuel supplies along the route even traveled by camel."
    ),
    MONA_LISA_STOLEN(
            1911, EventFamiliarity.RECOGNIZABLE,
            "the theft of the Mona Lisa from the Louvre",
            "Mona Lisa",
            "Vincenzo Peruggia took the painting from the Louvre. It was recovered in Italy two years later, after a worldwide sensation."
    ),
    FIRST_WORLD_WAR(
            1914, EventFamiliarity.FAMILIAR,
            "the beginning of the First World War",
            "World War I"
    ),
    TUTANKHAMUN_TOMB_DISCOVERED(
            1922, EventFamiliarity.FAMILIAR,
            "the discovery of Tutankhamun's tomb",
            "Discovery of the tomb of Tutankhamun"
    ),
    EMU_WAR(
            1932, EventFamiliarity.RECOGNIZABLE,
            "Australia's Emu War",
            "Emu War",
            "Soldiers with machine guns were deployed against crop-raiding emus. The birds proved far harder to control than officials expected."
    );

    private final int year;
    private final EventFamiliarity familiarity;
    private final String displayName;
    private final String wikipediaTitle;
    private final String summary;

    HistoricalEvent(int year, EventFamiliarity familiarity, String displayName, String wikipediaTitle) {
        this(year, familiarity, displayName, wikipediaTitle, null);
    }

    HistoricalEvent(int year, EventFamiliarity familiarity, String displayName, String wikipediaTitle, String summary) {
        this.familiarity = familiarity;
        this.year = year;
        this.displayName = displayName;
        this.wikipediaTitle = wikipediaTitle;
        this.summary = summary;
    }

    public EventFamiliarity getFamiliarity() {
        return familiarity;
    }

    public int getYear() {
        return year;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWikipediaUrl() {
        return "https://en.wikipedia.org/wiki/" + URLEncoder.encode(
                wikipediaTitle.replace(' ', '_'), StandardCharsets.UTF_8
        ).replace("+", "%20");
    }

    public String getSummary() {
        return summary;
    }
}
