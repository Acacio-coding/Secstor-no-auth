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
git clone -b noauth https://github.com/Acacio-coding/Secstor
```

Agora siga os seguintes passos:

1. Abra o projeto com a IDE ou editor de código de sua preferência;
2. Crie um arquivo chamado "<b>config.properties</b>" dentro da pasta <i><b>resources</b></i>, localizado em "<b>src/main</b>";
3. Adicione as seguintes linhas no arquivo criado adaptando os parâmetros para o seu ambiente:
```
server.port=PORTA DESEJADA

spring.datasource.url=jdbc:mysql://localhost:3306/NOME DO BANCO DE DADOS?createDatabaseIfNotExist=true
spring.datasource.username=USUÁRIO DO BANCO DE DADOS
spring.datasource.password=SENHA DO BANCO DE DADOS

secstor.n=NÚMERO DE CHAVES GERADAS NO SPLIT
secstor.k=NÚMERO MÍNIMO DE CHAVES UTILIZADAS NO RECONSTRUCT
```
Obs.: o endereço do banco de dados e a porta também podem mudar, mas para a execução em uma máquina local, pode se manter os mesmos utilizados acima.
4. A partir da ferramente realize um <i><b>build</b></i> para que os arquivos de código fonte sejam compilados e os arquivos de saída (para execução) sejam gerados;
5. Execute o projeto a partir da classe que contém o método <i><b>main</b></i>.

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