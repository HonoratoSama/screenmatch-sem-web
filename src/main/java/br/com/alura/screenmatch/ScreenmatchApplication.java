package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLOutput;
import java.util.Scanner;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String endereco1 = "https://www.omdbapi.com/?t=";
		String nomeSerie = null;
		String endereco2 = "&Season=";
		String temporada = null;
		String chaveApi  = "&apikey=fc184dd9";

		Scanner sc = new Scanner(System.in);


		System.out.println("Digite o Nome do Serie: ");
		nomeSerie = sc.nextLine().replaceAll(" ","_").toUpperCase();
		System.out.println(nomeSerie);
		System.out.println("Informe a Temporada: ");
		temporada = sc.nextLine();

		if (!nomeSerie.equals("") &&  !temporada.equals("") ) {
			var consumo = new ConsumoApi();
			var json = consumo.obterDados(endereco1 + nomeSerie + endereco2 + temporada + chaveApi);
			System.out.println(json);
			ConverteDados conversor = new ConverteDados();
			DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
			System.out.println(dados);
		} else {
			System.out.println("É preciso informar o Nome da Série e a temporada!");
		}
	}
}
