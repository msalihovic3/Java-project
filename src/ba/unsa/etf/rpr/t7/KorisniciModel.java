package ba.unsa.etf.rpr.t7;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class KorisniciModel {
    private ObservableList<Korisnik> korisnici = FXCollections.observableArrayList();
    private SimpleObjectProperty<Korisnik> trenutniKorisnik = new SimpleObjectProperty<>();

    private Connection conn;
    private PreparedStatement upitObrisi,upitDajKorisnike,upitPromijeni ,dodajKorisnikaUpit,odrediIdkorisikaUpit;


    public KorisniciModel() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:korisnici.db");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            upitDajKorisnike= conn.prepareStatement("SELECT korisnik.id, korisnik.ime, korisnik.prezime, korisnik.email,korisnik.username, korisnik.password FROM korisnik");
        } catch (SQLException e) {
            regenerisiBazu();
            try {
                upitDajKorisnike= conn.prepareStatement("SELECT korisnik.id, korisnik.ime, korisnik.prezime, korisnik.email,korisnik.username, korisnik.password FROM korisnik");

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        try {
            upitObrisi=conn.prepareStatement("DELETE FROM korisnik " +
                    "WHERE id=?");
            upitPromijeni=conn.prepareStatement("UPDATE korisnik SET ime=?, prezime=?, email=?, username=?, password=? WHERE id=?");
            dodajKorisnikaUpit = conn.prepareStatement("INSERT INTO korisnik VALUES(?,?,?,?,?,?)");
            odrediIdkorisikaUpit = conn.prepareStatement("SELECT MAX(id)+1 FROM korisnik");
      } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private void regenerisiBazu() {
        Scanner ulaz = null;
        try {
            ulaz = new Scanner(new FileInputStream("korisnici.db.sql"));
            String sqlUpit = "";
            while (ulaz.hasNext()) {
                sqlUpit += ulaz.nextLine();
                if ( sqlUpit.charAt( sqlUpit.length()-1 ) == ';') {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.execute(sqlUpit);
                        sqlUpit = "";
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            ulaz.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void obrisi(Korisnik korisnik){
        korisnici.remove(korisnik);
        try {
            upitObrisi.setInt(1, korisnik.getId());
            upitObrisi.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Korisnik dajKorisnikaIzResultSeta(ResultSet rs) throws SQLException {
        return new Korisnik( rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getInt(1));
    }

    public ArrayList<Korisnik> dajKorisnike() {
        ArrayList<Korisnik> lista =new ArrayList<>();
        try {
            ResultSet rs = upitDajKorisnike.executeQuery();
            while (rs.next()) {

                Korisnik korisnik = dajKorisnikaIzResultSeta(rs);
                korisnici.add(korisnik);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    public void napuni() {
       /* korisnici.add(new Korisnik("Vedran", "Ljubović", "vljubovic@etf.unsa.ba", "vedranlj", "test"));
        korisnici.add(new Korisnik("Amra", "Delić", "adelic@etf.unsa.ba", "amrad", "test"));
        korisnici.add(new Korisnik("Tarik", "Sijerčić", "tsijercic1@etf.unsa.ba", "tariks", "test"));
        korisnici.add(new Korisnik("Rijad", "Fejzić", "rfejzic1@etf.unsa.ba", "rijadf", "test"));*/

       korisnici.addAll(this.dajKorisnike());
       trenutniKorisnik.set(null);
    }

    public ObservableList<Korisnik> getKorisnici() {
        return korisnici;
    }

    public void setKorisnici(ObservableList<Korisnik> korisnici) {
        this.korisnici = korisnici;
    }

    public Korisnik getTrenutniKorisnik() {

        return trenutniKorisnik.get();
    }

    public Connection getConn() {
        return conn;
    }

    public SimpleObjectProperty<Korisnik> trenutniKorisnikProperty() {
        return trenutniKorisnik;
    }

    public void setTrenutniKorisnik(Korisnik trenutniKorisnik) {
   if(this.trenutniKorisnik.get()!=null)
        this.promijeniKorisnika(this.trenutniKorisnik.get());
        this.trenutniKorisnik.set(trenutniKorisnik);

    }

    public void setTrenutniKorisnik(int i) {

        this.trenutniKorisnik.set(korisnici.get(i));
    }

    public void promijeniKorisnika( Korisnik novo) {
        try {
            upitPromijeni.setString(1,novo.getIme() );
            upitPromijeni.setString(2, novo.getPrezime());
            upitPromijeni.setString(3, novo.getEmail());
            upitPromijeni.setString(4, novo.getUsername());
            upitPromijeni.setString(5, novo.getPassword());
            upitPromijeni.setInt(6, novo.getId());
            upitPromijeni.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void diskonektuj() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dodajKorisnika(Korisnik korisnik) {
        try {
            ResultSet rs = odrediIdkorisikaUpit.executeQuery();
            int id = 1;
            if (rs.next()) {
                id = rs.getInt(1);
            }

            dodajKorisnikaUpit.setInt(1, id);
            dodajKorisnikaUpit.setString(2, korisnik.getIme());
            dodajKorisnikaUpit.setString(3, korisnik.getPrezime());
            dodajKorisnikaUpit.setString(4, korisnik.getEmail());
            dodajKorisnikaUpit.setString(5, korisnik.getUsername());
            dodajKorisnikaUpit.setString(6, korisnik.getPassword());

            dodajKorisnikaUpit.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
