/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geb.transform.implicitassertions

import org.junit.runner.JUnitCore
import org.spockframework.compiler.SpockTransform
import spock.lang.Specification
import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION
import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
import org.junit.runner.Result
import org.junit.runner.notification.Failure

class SpockIntegrationSpec extends Specification {
	
	def makeSpecClass() {
		def invoker = new TransformTestHelper() {
			protected configure(TransformTestHelper.Transforms transforms) {
				transforms.add(new SpockTransform(), SEMANTIC_ANALYSIS)
				transforms.add(new EvaluatedClosureTransformation(), CANONICALIZATION)
			}
		}
		
		def file = new File(getClass().classLoader.getResource('ExampleSpec.text').toURI())
		invoker.parse(file)
	}

	def "transform works in a spec feature method"() {
		given:
		def specClass = makeSpecClass()

		when:
		Result result = JUnitCore.runClasses(specClass)

		then:
		result.failureCount == 1
		Failure failure = result.failures.first()
		Throwable cause = failure.exception.cause
		cause != null
		cause.message.contains "1 == 2"
	}

	def "transform works in a spec helper method"() {
		given:
		def specClass = makeSpecClass()

		when:
		specClass.newInstance().helperMethod()

		then:
		Throwable thrown = thrown()
		Throwable cause = thrown.cause
		cause.message.contains "3 == 4"
	}

}

