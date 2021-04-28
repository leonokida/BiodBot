package com.biodbot;

import com.biodbot.QueryParameters;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Arrays;

import com.xatkit.core.XatkitBot;
import com.xatkit.plugins.react.platform.ReactPlatform; //receber e enviar mensagens
import com.xatkit.plugins.react.platform.io.ReactEventProvider; //recebe eventos não textuais e os traduz para eventos compatíveis com Xatkit
import com.xatkit.plugins.react.platform.io.ReactIntentProvider; //recebe mensagens e as traduz para intenções
import com.xatkit.plugins.rest.platform.RestPlatform; //realizar requisições REST e gerenciar respostas
import com.xatkit.plugins.rest.platform.utils.ApiResponse;
import lombok.val; //usado como tipo de declaraçao de uma variável local em vez de realmente escrever o tipo (inferência de tipo)

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.type.TypeReference;

//O Xatkit incorpora um Java DSL interno para facilitar a definição do bot.
import static com.xatkit.dsl.DSL.mapping;
import static com.xatkit.dsl.DSL.city; //entidade
import static com.xatkit.dsl.DSL.eventIs; //retorna um predicado que avalia o evento recebido frente ao evento fornecido
import static com.xatkit.dsl.DSL.fallbackState; //cria um estado parcial de fallback, executado quando um evento recebido não corresponde com nenhuma condição de transição do estado atual
import static com.xatkit.dsl.DSL.intent; //cria uma intenção parcial. O objeto retornado fornece métodos para especificar sentenças de treino
import static com.xatkit.dsl.DSL.intentIs; //retorna um predicado que avalia uma intenção recebida frente a uma intenção fornecida
import static com.xatkit.dsl.DSL.model; //cria um modelo de bot parcial. O objeto retornado permite especificar estados, intenções e plataformas
import static com.xatkit.dsl.DSL.state; //cria um estado parcial. O objeto retornado fornece métodos para especificar o corpo do estado, suas transições e fallbacks
import static com.xatkit.dsl.DSL.any;
import static com.xatkit.dsl.DSL.integer; //entidade para código
import static com.xatkit.dsl.DSL.date; //entidade para data


public class BiodBot {

    //Substitui letras acentuadas por sem acento.
    public static String RemoveEspeciais(String Palavra) {
        System.out.println("Caiu na função!");
        String PalavraFinal = Palavra.replaceAll("Ã", "A");
        PalavraFinal = PalavraFinal.replaceAll("Á", "A");
        PalavraFinal = PalavraFinal.replaceAll("Â", "A");
        PalavraFinal = PalavraFinal.replaceAll("É", "E");
        PalavraFinal = PalavraFinal.replaceAll("Ê", "E");
        PalavraFinal = PalavraFinal.replaceAll("Í", "I");
        PalavraFinal = PalavraFinal.replaceAll("Ó", "O");
        PalavraFinal = PalavraFinal.replaceAll("Ô", "O");
        PalavraFinal = PalavraFinal.replaceAll("Õ", "O");
        PalavraFinal = PalavraFinal.replaceAll("Ú", "U");
        return PalavraFinal;   
    }

    public static void main(String[] args) throws FileNotFoundException {

        //Coleta das métricas do arquivo YAML
        File metricsfile = new File("src/main/java/com/biodbot/metrics.yaml");
        Scanner metricsfilereader = new Scanner(metricsfile);

        String[] metcodigo = new String[1000];
        String[] metdescricao = new String[1000];
        int mettamanhoCodigo = 0;
        int mettamanhoDescricao = 0;

        while(metricsfilereader.hasNextLine()) {
            String data = metricsfilereader.nextLine();
            if (data.contains("name:")) {
                String[] particao = data.split(": ");
                metcodigo[mettamanhoCodigo] = particao[1].substring(1, particao[1].length() - 1);
                mettamanhoCodigo++;
            }
            else if (data.contains("description:")) {
                String[] particao = data.split(": ");
                metdescricao[mettamanhoDescricao] = particao[1].substring(1, particao[1].length() - 1);
                mettamanhoDescricao++;                                        
            }
        }

        metricsfilereader.close();

        //Coleta das dimensões do arquivo YAML
        File dimensionsfile = new File("src/main/java/com/biodbot/dimensions.yaml");
        Scanner dimensionsfilereader = new Scanner(dimensionsfile);

        String[] dimcodigo = new String[1000];
        String[] dimdescricao = new String[1000];
        int dimtamanhoCodigo = 0;
        int dimtamanhoDescricao = 0;

        while(dimensionsfilereader.hasNextLine()) {
            String data = dimensionsfilereader.nextLine();
            if (data.contains("name:")) {
                String[] particao = data.split(": ");
                dimcodigo[dimtamanhoCodigo] = particao[1].substring(1, particao[1].length() - 1);
                dimtamanhoCodigo++;
            }
            else if (data.contains("description:")) {
                String[] particao = data.split(": ");
                dimdescricao[dimtamanhoDescricao] = particao[1].substring(1, particao[1].length() - 1);
                dimtamanhoDescricao++;                                        
            }
        }

        dimensionsfilereader.close();

        val greetings = intent("Greetings")
            .trainingSentence("Oi")
            .trainingSentence("Olá")
            .trainingSentence("Oi, tudo bem?")
            .trainingSentence("Bom dia")
            .trainingSentence("Boa tarde")
            .trainingSentence("Boa noite");

        val beginQuery = intent("BeginQuery")
            .trainingSentence("Sim")
            .trainingSentence("Iniciar busca")
            .trainingSentence("Buscar")
            .trainingSentence("Consultar")
            .trainingSentence("Buscar no BIOD")
            .trainingSentence("Consultar no BIOD")
            .trainingSentence("Iniciar consulta");

        //Métricas
        val metricList = mapping("METRICA").entry().value(metcodigo[0]).synonym(metdescricao[0]);

        for(int i = 1; i < mettamanhoCodigo; i++) {
            metricList.entry().value(metcodigo[i]).synonym(metdescricao[i]);
        }

        val metric = intent("Metric")
            .trainingSentence("METRICA")
            .parameter("metrica").fromFragment("METRICA").entity(metricList);

        //Dimensões
        val dimensionList = mapping("DIMENSAO").entry().value(dimcodigo[0]).synonym(dimdescricao[0]);

        for (int i = 1; i < dimtamanhoCodigo; i++) {
            dimensionList.entry().value(dimcodigo[i]).synonym(dimdescricao[i]);
        }

        val dimension = intent("Dimension")
            .trainingSentence("DIMENSAO")
            .parameter("dimensao").fromFragment("DIMENSAO").entity(dimensionList);

        //Filtros
        val useFilter = intent("UseFilter")
            .trainingSentence("Sim");
        
        val dontUseFilter = intent("DontUseFilter")
            .trainingSentence("Não");

        val filter = intent("Filter")
            .trainingSentence("FILTRO")
            .parameter("filtro").fromFragment("FILTRO").entity(any());

        val cancel = intent("Cancel")
            .trainingSentence("Cancelar")
            .trainingSentence("Esquece")
            .trainingSentence("Cancelar busca")
            .trainingSentence("Cancelar consulta");

        ReactPlatform reactPlatform = new ReactPlatform();
        RestPlatform restPlatform = new RestPlatform();
        ReactEventProvider reactEventProvider = reactPlatform.getReactEventProvider();
        ReactIntentProvider reactIntentProvider = reactPlatform.getReactIntentProvider();

        //Objeto que guarda os parâmetros da busca
        QueryParameters queryParameters = new QueryParameters();

        //Declaração dos estados:
        val init = state("Init");
        val awaitingInput = state("AwaitingInput");
        val handleWelcome = state("HandleWelcome");
        val handleBeginQuery = state("HandleBeginQuery");
        val awaitingMetric = state("AwaitingMetric");
        val handleMetric = state("HandleMetric");
        val awaitingDimension = state("AwaitingDimension");
        val handleDimension = state("HandleDimension");
        val awaitingUseFilter = state("AwaitingUseFilter");
        val handleUseFilter = state("HandleUseFilter");
        val queryWithoutFilter = state("QueryWithoutFilter");
        val awaitingFilterType = state("AwaitingFilterType");
        val handleFilterType = state("HandleFilterType");
        val awaitingFilter = state("AwaitingFilter");
        val queryWithFilter = state("QueryWithFilter");
        val handleCancel = state("HandleCancel");

        init
            .next()
            .when(eventIs(ReactEventProvider.ClientReady)).moveTo(handleWelcome);

        awaitingInput
            .next()
            .when(intentIs(greetings)).moveTo(handleWelcome)
            .when(intentIs(beginQuery)).moveTo(handleBeginQuery);

        handleWelcome
            .body(context -> reactPlatform.reply(context, "Olá, gostaria de fazer uma consulta ao BIOD?"))
            .next()
            .moveTo(awaitingInput);

        handleBeginQuery
            .body(context -> reactPlatform.reply(context, "Certo. Qual métrica você gostaria de usar?"))
            .next()
            .moveTo(awaitingMetric);

        awaitingMetric
            .next()
            .when(intentIs(metric)).moveTo(handleMetric)
            .when(intentIs(cancel)).moveTo(handleCancel)
            .fallback(context -> {
                reactPlatform.reply(context, "Desculpe, essa não é uma métrica válida para mim!");
            });

        handleMetric
            .body(context -> {
                queryParameters.setMetric((String) context.getIntent().getValue("metrica"));
                reactPlatform.reply(context, "Qual dimensão você gostaria de usar?");
            })
            .next()
            .moveTo(awaitingDimension);

        awaitingDimension
            .next()
            .when(intentIs(dimension)).moveTo(handleDimension)
            .when(intentIs(cancel)).moveTo(handleCancel)
            .fallback(context -> {
                reactPlatform.reply(context, "Desculpe, essa não é uma dimensão válida para mim!");
            });

        handleDimension
            .body(context -> {
                queryParameters.setDimension((String) context.getIntent().getValue("dimensao"));
                reactPlatform.reply(context, "Você gostaria de usar algum filtro na consulta?");
            })
            .next()
            .moveTo(awaitingUseFilter);

        awaitingUseFilter
            .next()
            .when(intentIs(dimension)).moveTo(handleFilterType)
            .when(intentIs(useFilter)).moveTo(handleUseFilter)
            .when(intentIs(dontUseFilter)).moveTo(queryWithoutFilter)
            .when(intentIs(cancel)).moveTo(handleCancel)
            .fallback(context -> {
                reactPlatform.reply(context, "Desculpe, não entendi!");
            });

        handleUseFilter
            .body(context -> reactPlatform.reply(context, "Qual filtro você quer usar?"))
            .next()
            .moveTo(awaitingFilterType);

        awaitingFilterType
            .next()
            .when(intentIs(dimension)).moveTo(handleFilterType)
            .when(intentIs(cancel)).moveTo(handleCancel)
            .fallback(context -> {
                reactPlatform.reply(context, "Desculpe, esse não é um filtro válido para mim!");
            });

        handleFilterType
            .body(context -> {
                queryParameters.setFilterType((String) context.getIntent().getValue("dimensao"));
                reactPlatform.reply(context, "Por qual valor você quer filtrar?");
            })
            .next()
            .moveTo(awaitingFilter);

        awaitingFilter
            .next()
            .when(intentIs(filter)).moveTo(queryWithFilter)
            .when(intentIs(cancel)).moveTo(handleCancel);

        queryWithoutFilter
            .body(context -> {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("metrics", queryParameters.getMetric());
                parameters.put("dimensions", queryParameters.getDimension());
                ApiResponse<JsonElement> response = restPlatform.getJsonRequest(context, 
                    "https://biod.c3sl.ufpr.br/api/v1/data", 
                    parameters, 
                    Collections.emptyMap(), 
                    Collections.emptyMap()
                );
                if (response.getStatus() == 200) {
                    JsonArray responseArray = response.getBody().getAsJsonArray();
                    int tamanhoResponseArray = responseArray.size();
                    if (tamanhoResponseArray == 0) {
                        reactPlatform.reply(context, "Desculpe, a consulta com esses parâmetros não funcionou.");
                    }
                    else {
                        for (int i = 0; i < tamanhoResponseArray; i++) {
                            JsonObject itemArray = responseArray.get(i).getAsJsonObject();
                            reactPlatform.reply(context,
                                itemArray.get(queryParameters.getDimension()) +
                                ": " +
                                itemArray.get(queryParameters.getMetric())
                            );
                        }
                    }
                }
                else {
                    reactPlatform.reply(context, "Desculpe, ocorreu um erro: " +  response.getStatus() + " " + response.getStatusText());
                }

            })
            .next()
            .moveTo(awaitingInput);

        queryWithFilter
            .body(context -> {

                queryParameters.setFilter((String) context.getIntent().getValue("filtro"));
                if (queryParameters.getFilterType().contains("cidade:nome")) {
                    queryParameters.setFilter(RemoveEspeciais(queryParameters.getFilter().toUpperCase()));
                }

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("metrics", queryParameters.getMetric());
                parameters.put("dimensions", queryParameters.getDimension());
                parameters.put("filters", queryParameters.getFilterType() + "==" + queryParameters.getFilter());
                ApiResponse<JsonElement> response = restPlatform.getJsonRequest(context, 
                    "https://biod.c3sl.ufpr.br/api/v1/data", 
                    parameters, 
                    Collections.emptyMap(), 
                    Collections.emptyMap()
                );
                if (response.getStatus() == 200) {
                    JsonArray responseArray = response.getBody().getAsJsonArray();
                    int tamanhoResponseArray = responseArray.size();
                    if (tamanhoResponseArray == 0) {
                        reactPlatform.reply(context, "Desculpe, a consulta com esses parâmetros não funcionou.");
                    }
                    else {
                        for (int i = 0; i < tamanhoResponseArray; i++) {
                            JsonObject itemArray = responseArray.get(i).getAsJsonObject();
                            reactPlatform.reply(context,
                                itemArray.get(queryParameters.getDimension()) +
                                ": " +
                                itemArray.get(queryParameters.getMetric())
                            );
                        }
                    }
                }
                
                else {
                    reactPlatform.reply(context, "Desculpe, ocorreu um erro: " +  response.getStatus() + " " + response.getStatusText());
                }
            })
            .next()
            .moveTo(awaitingInput);

        handleCancel
            .body(context -> reactPlatform.reply(context, "Ok!"))
            .next()
            .moveTo(awaitingInput);

        val defaultFallback = fallbackState()
            .body(context -> reactPlatform.reply(context, "Desculpe, não entendi."));

        val botModel = model()
            .usePlatform(reactPlatform)
            .usePlatform(restPlatform)
            .listenTo(reactEventProvider)
            .listenTo(reactIntentProvider)
            .initState(init)
            .defaultFallbackState(defaultFallback);

        Configuration botConfiguration = new BaseConfiguration();
        botConfiguration.setProperty("xatkit.message.delay", 500);
        //Configurações do Google DialogFlow
        /*
        botConfiguration.setProperty("xatkit.dialogflow.projectId", "");
        botConfiguration.setProperty("xatkit.dialogflow.credentials.path", "");
        botConfiguration.setProperty("xatkit.dialogflow.language", "pt-BR");
        botConfiguration.setProperty("xatkit.dialogflow.clean_on_startup", true);
        */

        XatkitBot xatkitBot = new XatkitBot(botModel, botConfiguration);
        xatkitBot.run();
    }
}