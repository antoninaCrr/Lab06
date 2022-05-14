package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	private List<Citta> leCitta;
	private List<Citta> best;

	public Model() {

		MeteoDAO dao = new MeteoDAO();
		this.leCitta = dao.getAllCitta(); // all'atto della creazione della classe Model riempio la lista 'leCitta' con tutte le citta estratte dal DB
	}

	// of course you can change the String output with what you think works best
	public Double getUmiditaMedia(int mese, String localita) {
		MeteoDAO dao = new MeteoDAO();
		return dao.getAvgRilevamentiLocalitaMese(mese, localita);
	}
	
	// of course you can change the String output with what you think works best
	public List<Citta> trovaSequenza(int mese) { // metodo di avvio della ricorsione
		List<Citta> parziale = new ArrayList<Citta>();
		this.best = null; // inizializzo la lista dichiarata come proprietà della classe
		
		MeteoDAO dao = new MeteoDAO();
		
		for(Citta c : leCitta) {
			c.setRilevamenti(dao.getAllRilevamentiLocalitaMese(mese, c.getNome())); // per ogni città setto la lista dei rilevamenti estratti dal DB
		}
		
		cerca(parziale,0); // metodo ricorsivo che riceve come param. la soluzione parziale e il livello della ricorsione
		
		return best;
	}

	private void cerca(List<Citta> parziale, int livello) {
		
		// prima verifica sulla condizione dei giorniTotali 
		if(livello == NUMERO_GIORNI_TOTALI) { 
			// caso terminale
			double costo = calcolaCosto(parziale);
			
			if(best == null || costo<calcolaCosto(best)) {
				best = new ArrayList<>(parziale);
			}
		}else { // se siamo qui è perchè non siamo ancora arrivati all'ultimo livello ricorsivo
			// caso intermedio
			for(Citta prova : leCitta) {
				if(aggiuntaValida(parziale,prova)) {
					parziale.add(prova);
					cerca(parziale,livello+1);
					parziale.remove(parziale.size()-1); // rimuovo l'ultima città aggiunta nella soluzione parziale
				}
			}
		}
		
	}

	private boolean aggiuntaValida(List<Citta> parziale, Citta prova) {
		
		// la citta è stata aggiunta un numero di volte minore rispetto al num max di visite possibili?
		int conta = 0;
		// contiamo quante volte la città 'prova' era già apparsa nell'attuale lista costruita fin qui
		for(Citta precedente : parziale) {
			if(precedente.equals(prova))
				conta++;
		}
		if(conta >= NUMERO_GIORNI_CITTA_MAX) {
			return false;
		}
		
		// il primo giorno di visite del tecnico sono aggiungibili tutte le citta
		if(parziale.size()==0) {
			return true;
		}
		
		// prima di cambiare città il tecnico deve soggiornare almeno 3 gg nella stessa
		if(parziale.size() == 1 || parziale.size()==2) {
			return parziale.get(parziale.size()-1).equals(prova);
			// se la città che stiamo provando ad aggiungere è diversa da quella visitata il giorno 1 e 2 non possiamo aggiungerla
		}
		
		// se il giorno precedente il tecnico era già nella medesima citta che sto cercando di aggiungere allora l'inserimento è valido
		if(parziale.get(parziale.size()-1).equals(prova)) {
			return true;
		}
		
		// controllo che il tecnico sia rimasto nella medesima citta almeno tre gg consecutivi prima di cambiare citta
		if(parziale.get(parziale.size()-1).equals(parziale.get(parziale.size()-2)) &&
					parziale.get(parziale.size()-2).equals(parziale.get(parziale.size()-3))) {
				return true;
		}
		
		// in tutti gli altri casi ritorno falso
		return false;
	}

	private double calcolaCosto(List<Citta> parziale) {
		double costo = 0.0;
		
		// calcolo la parte di costo relativa al valore dell'umidità registrata nel 'giorno'
		for(int giorno = 1; giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			Citta c = parziale.get(giorno-1); // index pari a giorno -1 poichè il for parte da 1
			double umidita = c.getRilevamenti().get(giorno-1).getUmidita();
			
			costo += umidita;
		}
		
		// aggiungo eventualemte la quota fissa pari a 100 euro se ne sussistono le ipotesi
		for(int giorno=2;giorno <= NUMERO_GIORNI_TOTALI; giorno++) {
			if(!parziale.get(giorno-1).equals(parziale.get(giorno-2))) {
				costo += COST;
			}
		}
		
		return costo;
	}

	public List<Citta> getLeCitta() {
		// TODO Auto-generated method stub
		return leCitta;
	}
	

}
