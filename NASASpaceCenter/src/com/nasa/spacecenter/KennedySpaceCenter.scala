package com.nasa.spacecenter

import org.apache.log4j._
import org.apache.spark._
import org.apache.spark.SparkContext._
import scala.io.Source
import java.io.FileNotFoundException
import java.io.IOException
import scala.io.Codec
import java.nio.charset.CodingErrorAction
import org.apache.spark.rdd.RDD
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KennedySpaceCenter {
  
  // Tipo de dado customizado 
  // guarda os dados pertinentes à solução 
  type NASAData = (String, String, String, Int, Int)
  
  def captarElementos(linha: String):NASAData = {
    
    val elementoVazio = ("","","",0,0)
    try{
      val elementos = linha.split(" - - ")
      if((elementos.length > 0) && (linha.indexOf(" - - ") > 0)) {
        val servidor = elementos(0)
        
        val tempoDelimitador1 = elementos(1).indexOf('[')
        val tempoDelimitador2 = elementos(1).indexOf(']')
        val tempo = elementos(1).substring(tempoDelimitador1+1, tempoDelimitador2)
        
        var outraParte = elementos(1).substring(tempoDelimitador2, elementos(1).length())
        if(outraParte.indexOf('"') == 2){  //teste de sanidade
          outraParte = outraParte.substring(outraParte.indexOf('"')+1, outraParte.length())  
          val requisicao = outraParte.split('"')(0)
          
          val restante = outraParte.split('"')(1).trim().split(" ")
          if(restante.length==2){         //teste de sanidade    
            var totalBytes = restante(1)
            if(totalBytes.equals("-")){
              totalBytes = "0"
            }
            return (servidor, tempo, requisicao, restante(0).toInt, totalBytes.toInt)
          }     
          return elementoVazio
        }
        return elementoVazio
      }
      return elementoVazio
    } catch {
      case e: Exception => println("Erro ao processar esta linha: " + linha)
      return elementoVazio
    }
  }  
  
   /** Construir o RDD Inicial */
  def criarRddInicial(sc:SparkContext):RDD[NASAData] = {

    // Aplicar encoding UTF-8 para contornar erro na recuperação do arquivo do servidor FTP
    //Obs - retirei.. curiosamente enfrentei um erro no início mas não está mais acontecendo
    implicit val codec = Codec("ASCII")
    //codec.onMalformedInput(CodingErrorAction.REPLACE)
    //codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
    
    //adicionando usuário anonymous com qualquer senha para evitar erro de credencials em FTP 
    val fileURL1 = "ftp://anonymous:bigdata@ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz"
    val fileURL2 = "ftp://anonymous:bigdata@ita.ee.lbl.gov/traces/NASA_access_log_Aug95.gz"
     
    //artifício para contornar erro de "Login failed on server"
     sc.addFile(fileURL1)
     val fileName1 = SparkFiles.get(fileURL1.split("/").last)
     sc.addFile(fileURL2)
     val fileName2 = SparkFiles.get(fileURL2.split("/").last)    
     
     val linhas1 = sc.textFile(fileName1)
     val linhas2 = sc.textFile(fileName2)     
     val rdds = Seq(linhas1, linhas2)
     val allRDDs = sc.union(rdds)
     val conjuntos = allRDDs.map(captarElementos)
     
     return conjuntos
  } 
  
  /** Função main - onde dispara-se a operação */
  def main(args: Array[String]) {
    
    //Setar o nível de logs
    Logger.getLogger("org").setLevel(Level.ERROR)
    val log = Logger.getLogger("org")
    log.info("INÍCIO")        
    
    //Criar SparkContext usando todos os cores
    val sc = new SparkContext("local[*]", "KennedySpaceCenter")
    
    //Construir o RDD
    val iteracaoRDD = criarRddInicial(sc)    
    
    // Contar número de servidores requisitantes únicos
    val servidorContagemMap = iteracaoRDD.map(x => (x._1, 1))
    val servidorContagemRbk = servidorContagemMap.reduceByKey(_+_)
    val servidorContagemCnt = servidorContagemRbk.count()
    println(s"Número de hosts únicos: $servidorContagemCnt")
    
  // Contar número de erros 404
    val erros404Map = iteracaoRDD.map(x => (x._4, 1))
    val erros404Ftr = erros404Map.filter(x => x._1.toInt == 404)
    val erros404Cnt = erros404Ftr.count()
    println(s"Total de erros 404: $erros404Cnt")
    
    //Listar as 5 URLs que mais causaram erro 404
    val topoURLErrosFtr = iteracaoRDD.filter(x => x._4.toInt == 404)   
    val topoURLErrosMap = topoURLErrosFtr.map(x => (x._1, 1))
    val topoURLErrosRbk = topoURLErrosMap.reduceByKey(_+_)
    val topoURLErrosTop = topoURLErrosRbk.map(x => (x._2, x._1)).sortByKey().top(5)
    println("As 5 URLs que mais causaram erro 404:")
    var z:Int = 0
    while ((z < 5) && (z < topoURLErrosTop.size)) { 
      val topo1 = topoURLErrosTop(z)._1
      val topo2 = topoURLErrosTop(z)._2
      var k = z+1
      println(s"$k: $topo2 com $topo1 erros")
      z += 1
    }
    
    //Listar a quantidade de erros 404 por dia:
    val erros404Dia = iteracaoRDD.filter(x => x._4.toInt == 404)
    val erros404DiaMap = erros404Dia.map(x => ({x._2.substring(0, 11)}, 1))
    val erros404DiaRbk = erros404DiaMap.reduceByKey(_+_).map(x => (x._2, x._1)).sortByKey(false) collect()
    println("Quantidade de erros 404 por dia:")    
     for (resultado <- erros404DiaRbk) {
       val erroDia2 = resultado._1
       val erroDia1 = resultado._2       
       //val s:String = erroDia2
       //val simpleDateFormat:SimpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.US);
       //val data:Date = simpleDateFormat.parse(s);       
       println(s"$erroDia1 com $erroDia2 erros")       
   }
    
    //contar o total de bytes
    val totalytesSum:Long = iteracaoRDD.map(x => x._5).sum.toLong
    println(s"Total de bytes retornados: $totalytesSum")    
    
    log.info("TÉRMINO")
    
  }
  
}