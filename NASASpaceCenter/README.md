Teste Semantix - Fernando Wenzel Neumann - 01/06/2018

Qual o objetivo do comando cache em Spark?
Seu objetivo é persistir o RDD em memória, como exemplo, dos dados obtidos com o instanciamento de um SparkContext ("sc") a partir de um arquivo. 
A leitura de um arquivo (val arquivo = sc.textFile("<...>")) não é executada imediatamente, somente após invocação de comandos sobre o RDD tais como count e top, os quais causam processamento em todos os datanodes envolvidos sempre que chamados. O cache (.cache()) faz com que somente a primeira chamada seja feita e então armazenadas em cache para poupar processamento, mas depende de espaço disponível em memória. Caso não tenha, o comando cache será ignorado.

O mesmo código implementado em Spark é normalmente mais rápido que a implementação equivalente em MapReduce. Por quê?
Porque Spark inovou com os dados sendo distribuídos em memória versus em disco, que é a solução MapReduce, podendo possibilitar vantagem de desempenho na casa das dezenas, chegado bem próximo ao tempo real.

Qual é a função do SparkContext ?
O SparkContext é o primeiro e primordial passo para se criar e levantar uma aplicação de Spark. Por convenção, ela é instanciada com o nome "sc".
Através dele, acessa-se o cluster Spark. Outro conceito relacionado a ele e usado em conjunto é o SparkConf, onde se configura propriedades diversas tais como a referência ao Spark resource manager e a quantidade de RAM e de cores.

Explique com suas palavras o que é Resilient Distributed Datasets (RDD)
RDD vem a ser a unidade fundamental da estrutura de dados de Spark, sobre o qual uma solução em Spark se desenvolve. Uma vez instanciado, sua respectiva coleção de objetos é imutável num cluster sobre o qual roda, e sobre os nós deste cluster fica particionado e distribuído para então se obter o processamento paralelo com as operações feitas sobre o RDD.

RDD (em português):
Resiliente - se os dados em memória forem perdidos, podem ser recriados;
Distribuído - armazenado em memória pelos nós do cluster;
Coleção de objetos - podem vir de arquivo ou criados num programa Spark (da memória ou de outro RDD).

GroupByKey é menos eficiente que reduceByKey em grandes dataset. Por quê?
Porque ele agrupa todos elementos chave-valor sob mesma chave a partir de todos as partições, gerando tráfego excessivo na rede, e executando um reduce final a partir disso em um dos datanodes que escolhe, enquanto que o reduceByKey já executa um prévio processamento de reduce em cada partição finalizando num último reduce.




Explique o que o código Scala abaixo faz.


val textFile = sc . textFile ( "hdfs://..." )
val counts = textFile . flatMap ( line => line . split ( " " ))
. map ( word => ( word , 1 ))
. reduceByKey ( _ + _ )
counts . saveAsTextFile ( "hdfs://..." )

A instância de RDD textFile é criada com o objeto do SparkContext a partir de arquivo presente dentro de um caminho no Hadoop.
A instância de RDD counts é criada a partir da outra (textFile), onde flatMap executa uma função que retornará nenhum, uma ou mais elementos do RDD criados a partir de delimitação com o caractere " "
A operação map mapeia cada elemento formado para o número 1, formando conseqüentemente elementos "chave-valor" com todas as chaves (isto é, neste caso,  elementos) presentes.
A operação reduceByKey faz exatamente o que indica com seu nome: reduz por chave, ou seja, para cada chave encontrada (com repetições eliminadas), será seguida do número de vezes que se repetiu.
A instância de RDD counts é gravada em formato de arquivo de texto no caminho Hadoop indicado.





HTTP requests to the NASA Kennedy Space Center WWW server

O código-fonte da minha solução encontra-se no seguinte repositório GitHub:
https://github.com/fer2200/NASASpaceCenter

Questões
Responda as seguintes questões devem ser desenvolvidas em Spark utilizando a sua linguagem de preferência.

1. Número de hosts únicos.
137978

2. O total de erros 404.
20871

3. Os 5 URLs que mais causaram erro 404.
1: hoohoo.ncsa.uiuc.edu com 251 erros
2: piweba3y.prodigy.com com 156 erros
3: jbiagioni.npt.nuwc.navy.mil com 132 erros
4: piweba1y.prodigy.com com 114 erros
5: www-d4.proxy.aol.com com 91 erros

4. Quantidade de erros 404 por dia.
Obs: preferi ordenar por quantidade de erros.

06/Jul/1995 com 640 erros
19/Jul/1995 com 638 erros
30/Aug/1995 com 571 erros
07/Jul/1995 com 569 erros
07/Aug/1995 com 537 erros
13/Jul/1995 com 531 erros
31/Aug/1995 com 526 erros
05/Jul/1995 com 497 erros
11/Jul/1995 com 471 erros
03/Jul/1995 com 470 erros
12/Jul/1995 com 470 erros
18/Jul/1995 com 465 erros
25/Jul/1995 com 461 erros
20/Jul/1995 com 428 erros
29/Aug/1995 com 420 erros
24/Aug/1995 com 420 erros
25/Aug/1995 com 415 erros
14/Jul/1995 com 411 erros
28/Aug/1995 com 410 erros
17/Jul/1995 com 406 erros
10/Jul/1995 com 398 erros
08/Aug/1995 com 390 erros
06/Aug/1995 com 373 erros
27/Aug/1995 com 367 erros
26/Aug/1995 com 366 erros
04/Jul/1995 com 359 erros
09/Jul/1995 com 348 erros
04/Aug/1995 com 346 erros
23/Aug/1995 com 345 erros
27/Jul/1995 com 336 erros
26/Jul/1995 com 336 erros
21/Jul/1995 com 332 erros
24/Jul/1995 com 328 erros
15/Aug/1995 com 327 erros
01/Jul/1995 com 316 erros
20/Aug/1995 com 312 erros
10/Aug/1995 com 306 erros
21/Aug/1995 com 305 erros
03/Aug/1995 com 303 erros
08/Jul/1995 com 302 erros
02/Jul/1995 com 291 erros
14/Aug/1995 com 287 erros
22/Aug/1995 com 285 erros
09/Aug/1995 com 279 erros
17/Aug/1995 com 271 erros
11/Aug/1995 com 263 erros
16/Aug/1995 com 259 erros
16/Jul/1995 com 257 erros
18/Aug/1995 com 256 erros
15/Jul/1995 com 254 erros
01/Aug/1995 com 243 erros
05/Aug/1995 com 236 erros
23/Jul/1995 com 233 erros
13/Aug/1995 com 216 erros
19/Aug/1995 com 209 erros
12/Aug/1995 com 196 erros
22/Jul/1995 com 191 erros
28/Jul/1995 com 94 erros

5. O total de bytes retornados.
65524307881 (long datatype)
