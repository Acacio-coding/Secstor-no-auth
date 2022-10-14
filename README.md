# Secstor

O objetivo é desenvolver uma biblioteca ou uma api para a utilização de algoritmos de compartilhamento de segredos e anonimização de dados para adequação de sistemas à LGPD.

## Requerimentos

- JDK 17 ou superior
- JRE 1.8.0 ou superior
- MySQL 8.0.27 ou superior
- Lombok Annotations Support for VS Code (extensão para o <b>Visual Studio Code</b>)

## Para utilizar o projeto

Utilize o comando abaixo:

```
git clone https://github.com/Acacio-coding/Secstor-no-auth
```

Agora siga os seguintes passos:

1. Abra o projeto com a IDE ou editor de código de sua preferência;
2. Crie um arquivo chamado "<b>config.properties</b>" dentro da pasta <i><b>resources</b></i>, localizado em "<b>src/main</b>";
3. Adicione as seguintes linhas no arquivo criado adaptando os parâmetros para o seu ambiente:
```properties
server.port=PORTA DESEJADA

spring.datasource.url=jdbc:mysql://localhost:3306/NOME DO BANCO DE DADOS?createDatabaseIfNotExist=true
spring.datasource.username=USUÁRIO DO BANCO DE DADOS
spring.datasource.password=SENHA DO BANCO DE DADOS

secstor.n=NÚMERO DE CHAVES GERADAS NO SPLIT
secstor.k=NÚMERO MÍNIMO DE CHAVES UTILIZADAS NO RECONSTRUCT
```
4. Caso esteja utilizando docker, utilize o comando abaixo, para que o container do banco de dados seja criado:

```
docker-compose up --build --force-recreate
```
5. A partir da ferramente realize um <i><b>build</b></i> para que os arquivos de código fonte sejam compilados e os arquivos de saída (para execução) sejam gerados;
6. Execute o projeto a partir da classe que contém o método <i><b>main</b></i>.

### Request throttling

No arquivo **application.properties** localizado em **src/main/resources** as seguintes linhas correspondem a configuração do **bucket4j**
para a gestão de **ratelimit** em cada request, é possível configurá-lo a partir do seguinte exemplo:

```properties
#bucket4j
bucket4j.enabled=true
bucket4j.filters[0].cache-name=rateLimit
#endpoints onde o filtro de ratelimiting é aplicado
bucket4j.filters[0].url=/api/v1/*
bucket4j.filters[0].filter-method=servlet
bucket4j.filters[0].filter-order=100
#expressão para validar para quem o filtro deve ser aplicado, por padrão para cada endereço de IP
bucket4j.filters[0].rate-limits[0].expression="getRemoteAddr()"
#condição de exclusão ao filtro, por padrão o ip local não é afetado pelo ratelimiting
bucket4j.filters[0].rate-limits[0].skip-condition=getRemoteAddr() == '127.0.0.1'
#número máximo de requests por tempo, por padrão 20 requests
bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=20
#tempo para o número máximo de requests, por padrão a cada 1 hora
bucket4j.filters[0].rate-limits[0].bandwidths[0].time=1
bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=hours
```

## Para realizar testes de tempo

Siga os seguintes passos:

1. Certifique-se de estar na pasta raíz do projeto, exemplo "<i><b>C:/Users/User/Desktop/secstorNoAuth</b></i>";
2. Execute o arquivo "<b>timing-test-runner.sh</b>" no terminal do seu sistema, no windows pode ser utilizado até mesmo o bash do Git;
3. Siga os passos requeridos pelo script.

Obs.: a execução do script gera resultados para um teste com N=x, K=y, um dos datasets presentes na pasta "<b>timing-tests/datasets/split</b>", número de chaves usadas no reconstruct e número de threads executando o teste paralelamente.
Os resultados serão escritos em formato .csv e será gerado um dataset para reconstrução que pode ser utilizado nas rotas de reconstruct da própria api.


## Referências
T. Loruenser, A. Happe, D. Slamanig: "ARCHISTAR: Towards Secure and Robust Cloud Based Data Sharing"; Vortrag: Cloud Computing Technology and Science (CloudCom), 2015, Vancouver, Canada; 30.11.2015 - 03.12.2015; in: "CloudCom 2015", IEEE, (2016), S. 371 - 378.

Disponível em: <https://github.com/Archistar/archistar-smc>