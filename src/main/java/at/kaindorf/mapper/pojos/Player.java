package at.kaindorf.mapper.pojos;

import at.kaindorf.mapper.annotations.XmlAttribute;
import at.kaindorf.mapper.annotations.XmlElement;
import at.kaindorf.mapper.annotations.XmlFormat;
import at.kaindorf.mapper.xml.XmlMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Project: plf_xmlMapper
 * Created by: SF
 * Date: 16.03.2024
 * Time: 11:19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Player {
  @XmlAttribute
  @XmlElement(name = "player_id")
  private Long playerId;
  private String nickname;
  private int rank;
  @XmlAttribute
  private double rating;
  private Float experience;
  @XmlAttribute
  private LocalDate since;
  @XmlFormat(pattern = "dd/MM/yyyy")
  private LocalDate birthdate;

  public static void main(String[] args) {
    XmlMapper mapper = new XmlMapper();
    Player player = new Player(123L, "Steel", 3, 9.4567, 4.5f, LocalDate.of(2021,4,16),
        LocalDate.of(2004,6,3));
    System.out.println(player);
    File xmlFile = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "player.xml").toFile();
    try {
      mapper.marshall(player, xmlFile);
      Player steel = (Player) mapper.unmarshall(Player.class, xmlFile);
      System.out.println(steel);
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }
}
