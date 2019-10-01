package ai.lum.odinson

import org.scalatest._

class TestMoreEvents extends FlatSpec with Matchers {

  import TestEvents._

  val json = """{"id":"3c1237c6-01d0-42a4-9459-c84fed223286","metadata":[],"sentences":[{"numTokens":9,"fields":[{"$type":"ai.lum.odinson.TokensField","name":"raw","tokens":["John","ate","ramen","with","chopsticks","and","a","spoon","."],"store":true},{"$type":"ai.lum.odinson.TokensField","name":"word","tokens":["John","ate","ramen","with","chopsticks","and","a","spoon","."]},{"$type":"ai.lum.odinson.TokensField","name":"tag","tokens":["NNP","VBD","NNS","IN","NNS","CC","DT","NN","."]},{"$type":"ai.lum.odinson.TokensField","name":"lemma","tokens":["john","eat","raman","with","chopstick","and","a","spoon","."]},{"$type":"ai.lum.odinson.TokensField","name":"entity","tokens":["I-PER","O","O","O","O","O","O","O","O"]},{"$type":"ai.lum.odinson.TokensField","name":"chunk","tokens":["B-NP","B-VP","B-NP","B-PP","B-NP","O","B-NP","I-NP","O"]},{"$type":"ai.lum.odinson.GraphField","name":"dependencies","incomingEdges":[[[1,"nsubj"]],[],[[1,"dobj"]],[[4,"case"]],[[1,"nmod_with"]],[[4,"cc"]],[[7,"det"]],[[4,"conj"]],[[1,"punct"]]],"outgoingEdges":[[],[[0,"nsubj"],[2,"dobj"],[4,"nmod_with"],[8,"punct"]],[],[],[[3,"case"],[5,"cc"],[7,"conj"]],[],[],[[6,"det"]],[]],"roots":[1]}]},{"numTokens":7,"fields":[{"$type":"ai.lum.odinson.TokensField","name":"raw","tokens":["Daisy","ate","macaroni","at","her","house","."],"store":true},{"$type":"ai.lum.odinson.TokensField","name":"word","tokens":["Daisy","ate","macaroni","at","her","house","."]},{"$type":"ai.lum.odinson.TokensField","name":"tag","tokens":["NNP","VBD","NNS","IN","PRP$","NN","."]},{"$type":"ai.lum.odinson.TokensField","name":"lemma","tokens":["daisy","eat","macaroni","at","her","house","."]},{"$type":"ai.lum.odinson.TokensField","name":"entity","tokens":["I-PER","O","O","O","O","O","O"]},{"$type":"ai.lum.odinson.TokensField","name":"chunk","tokens":["B-NP","B-VP","B-NP","B-PP","B-NP","I-NP","O"]},{"$type":"ai.lum.odinson.GraphField","name":"dependencies","incomingEdges":[[[1,"nsubj"]],[],[[1,"dobj"]],[[5,"case"]],[[5,"nmod:poss"]],[[1,"nmod_at"]],[[1,"punct"]]],"outgoingEdges":[[],[[0,"nsubj"],[5,"nmod_at"],[2,"dobj"],[6,"punct"]],[],[],[],[[3,"case"],[4,"nmod:poss"]],[]],"roots":[1]}]},{"numTokens":15,"fields":[{"$type":"ai.lum.odinson.TokensField","name":"raw","tokens":["Gus","'s","pets","include","cats",",","dogs",",","parakeets",",","ponies",",","and","unicorns","."],"store":true},{"$type":"ai.lum.odinson.TokensField","name":"word","tokens":["Gus","'s","pets","include","cats",",","dogs",",","parakeets",",","ponies",",","and","unicorns","."]},{"$type":"ai.lum.odinson.TokensField","name":"tag","tokens":["NNP","POS","NNS","VBP","NNS",",","NNS",",","NNS",",","NNS",",","CC","NNS","."]},{"$type":"ai.lum.odinson.TokensField","name":"lemma","tokens":["gus","s","pet","include","cat",",","dog",",","parakeet",",","pony",",","and","unicorn","."]},{"$type":"ai.lum.odinson.TokensField","name":"entity","tokens":["I-LOC","O","O","O","O","O","O","O","O","O","O","O","O","O","O"]},{"$type":"ai.lum.odinson.TokensField","name":"chunk","tokens":["B-NP","B-NP","I-NP","B-VP","B-NP","O","B-NP","O","B-NP","O","B-NP","O","O","B-NP","O"]},{"$type":"ai.lum.odinson.GraphField","name":"dependencies","incomingEdges":[[[2,"nmod:poss"]],[[0,"case"]],[[3,"nsubj"]],[],[[3,"dobj"]],[[4,"punct"]],[[3,"dobj"],[4,"conj"]],[[4,"punct"]],[[3,"dobj"],[4,"conj"]],[[4,"punct"]],[[3,"dobj"],[4,"conj"]],[[4,"punct"]],[[4,"cc"]],[[3,"dobj"],[4,"conj"]],[[3,"punct"]]],"outgoingEdges":[[[1,"case"]],[],[[0,"nmod:poss"]],[[2,"nsubj"],[4,"dobj"],[6,"dobj"],[8,"dobj"],[10,"dobj"],[13,"dobj"],[14,"punct"]],[[5,"punct"],[6,"conj"],[7,"punct"],[8,"conj"],[9,"punct"],[10,"conj"],[11,"punct"],[12,"cc"],[13,"conj"]],[],[],[],[],[],[],[],[],[],[]],"roots":[3]}]}]}"""
  val doc = Document.fromJson(json)
  val ee = TestUtils.mkExtractorEngine(doc)

  "Odinson" should "find two events with one tool each" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs.head.matches should have size 2
    val Array(m1, m2) = results.scoreDocs.head.matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    testEventTrigger(m2, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("tool", 4, 5))
    val desiredArgs2 = Seq(Argument("theme", 2, 3), Argument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    testEventArguments(m2, desiredArgs2)
  }

  it should "find one events with two tools" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool+ = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs.head.matches should have size 1
    val Array(m1) = results.scoreDocs.head.matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("tool", 4, 5), Argument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
  }

  it should "find two events, one with two tools, and one with zero" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool* = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (2)
    results.scoreDocs(0).matches should have size 1
    val Array(m1) = results.scoreDocs(0).matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("tool", 4, 5), Argument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    results.scoreDocs(1).matches should have size 1
    val Array(m2) = results.scoreDocs(1).matches
    // test trigger
    testEventTrigger(m2, start = 1, end = 2)
    // test arguments
    val desiredArgs2 = Seq(Argument("theme", 2, 3))
    testEventArguments(m2, desiredArgs2)
  }

  it should "find two events with one tool each even if theme is optional" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food? = >dobj
      tool: ^tool = >nmod_with >conj?
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs.head.matches should have size 2
    val Array(m1, m2) = results.scoreDocs.head.matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    testEventTrigger(m2, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("tool", 4, 5))
    val desiredArgs2 = Seq(Argument("theme", 2, 3), Argument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    testEventArguments(m2, desiredArgs2)
  }

  it should "not find events with both tool and location" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool = >nmod_with >conj?
      location: ^place = >nmod_at
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (0)
  }

  it should "find three events when theme, tool, and location are optional" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food? = >dobj
      tool: ^tool? = >nmod_with >conj?
      location: ^place? = >nmod_at
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (2)
    // sentence 1
    results.scoreDocs(0).matches should have size 2
    val Array(m1, m2) = results.scoreDocs(0).matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    testEventTrigger(m2, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("tool", 4, 5))
    val desiredArgs2 = Seq(Argument("theme", 2, 3), Argument("tool", 7, 8))
    testEventArguments(m1, desiredArgs1)
    testEventArguments(m2, desiredArgs2)
    // sentence 2
    results.scoreDocs(1).matches should have size 1
    val Array(m3) = results.scoreDocs(1).matches
    // test trigger
    testEventTrigger(m3, start = 1, end = 2)
    // test arguments
    val desiredArgs3 = Seq(Argument("theme", 2, 3), Argument("location", 5, 6))
    testEventArguments(m3, desiredArgs3)
  }

  it should "find one event with required location" in {
    val pattern = """
      trigger = [lemma=eat]
      theme: ^food = >dobj
      tool: ^tool? = >nmod_with >conj?
      location: ^place = >nmod_at
    """
    val q = ee.compiler.compileEventQuery(pattern)
    val results = ee.query(q, 5)
    results.totalHits should equal (1)
    results.scoreDocs(0).matches should have size 1
    val Array(m1) = results.scoreDocs(0).matches
    // test trigger
    testEventTrigger(m1, start = 1, end = 2)
    // test arguments
    val desiredArgs1 = Seq(Argument("theme", 2, 3), Argument("location", 5, 6))
    testEventArguments(m1, desiredArgs1)
  }

  // TODO test argument ranges using sentence 3

}
