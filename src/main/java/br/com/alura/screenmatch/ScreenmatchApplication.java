package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import ch.qos.logback.core.encoder.JsonEscapeUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

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
        String endereco3 = "&episode=";
        String episodio = null;
        String chaveApi = "&apikey=fc184dd9";

        Scanner sc = new Scanner(System.in);

        System.out.println("Digite o Nome do Serie: ");
        nomeSerie = sc.nextLine().replaceAll(" ", "_").toUpperCase();
        System.out.println(nomeSerie);
        System.out.println("Informe a Temporada: ");
        temporada = sc.nextLine();
        System.out.println("Informe o Episódio: ");
        episodio = sc.nextLine();

        if (!nomeSerie.equals("") && !temporada.equals("")) {
            var consumo = new ConsumoApi();
            //var json = consumo.obterDados(endereco1 + nomeSerie + endereco2 + temporada + chaveApi);
            var json = consumo.obterDados(endereco1 + nomeSerie + chaveApi);
            //System.out.println(json);
            ConverteDados conversor = new ConverteDados();
            DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
            System.out.println(dados);


            var consumo2 = new ConsumoApi();
            int statusCode = consumo2.obterStatusCode(endereco1 + nomeSerie + chaveApi);
            System.out.println("********" + statusCode + "********");
            if (statusCode == 200) {
                List<DadosTemporada> temporadas = new ArrayList<>();
                try {
                    for (int i = 1; i <= dados.totalTemporadas(); i++) {
                        json = consumo.obterDados(endereco1 + nomeSerie + endereco2 + i + chaveApi);
                        //System.out.println(json);
                        //ConverteDados conversor1 = new ConverteDados();
                        DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                        temporadas.add(dadosTemporada);
                    }
                } catch (NullPointerException e) {
                    System.out.println("Serie não encontrada: " + nomeSerie);
                }

                temporadas.forEach(t -> {
                    System.out.println("Temporada: " + t.numero());
                    DoubleAdder numeros = new DoubleAdder();
                    t.episodios().forEach(e -> {
                        numeros.add(Double.parseDouble(e.avaliacao().replaceAll("N/A", "0.0")));
                        System.out.println(" " + e);
                    });
                    double mMedia = (numeros.doubleValue() / t.episodios().size());
                    System.out.printf(" Nota da Avaliação da Temporada: %.1f%n", mMedia);
                });

                List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                        .flatMap(t -> t.episodios().stream())
                        .collect(Collectors.toList());

                System.out.println("\nTop 5 episódios:");
                dadosEpisodios.stream()
                        .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                        .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                        .limit(5)
                        .forEach(System.out::println);

                System.out.println("\nTop 5 episódios com Temporadas: ");
                List<Episodio> episodiosx = temporadas.stream()
                        .flatMap(t -> t.episodios().stream()
                                .map(d -> new Episodio(t.numero(), d))
                        )
                        .filter(d -> !d.getAvaliacao().equals("N/A"))
                        .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                        .limit(5)
                        .collect(Collectors.toList());

                episodiosx.forEach(System.out::println);

                System.out.println("Episódios à partir do Ano: ");
                var ano = sc.nextInt();
                sc.nextLine();

                LocalDate dataBusca = LocalDate.of(ano, 1, 1);
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                episodiosx.stream()
                        .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                        .forEach(e -> System.out.println(
                                "Temporada: " + e.getTemporada() +
                                " Episódio: " + e.getTitulo() +
                                "Data Lançamento: " + e.getDataLancamento().format(formatador)));


            } else {
                System.out.println("Status Code: " + statusCode);
            }
        } else {
            System.out.println("É preciso informar o Nome da Série e a temporada!");
        }
    }
}
