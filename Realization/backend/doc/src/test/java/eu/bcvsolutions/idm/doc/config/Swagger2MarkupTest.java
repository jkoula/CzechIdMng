
package eu.bcvsolutions.idm.doc.config;

import org.junit.Test;

import eu.bcvsolutions.idm.doc.DocModuleDescriptor;
import eu.bcvsolutions.idm.test.api.AbstractSwaggerTest;


/**
 * Static swagger generation to sources - will be used as input for swagger2Markup build
 *
 * @author Jirka Koula
 *
 */
public class Swagger2MarkupTest extends AbstractSwaggerTest {

	@Test
	public void testConvertSwagger() throws Exception {
		super.convertSwagger(DocModuleDescriptor.MODULE_ID);
	}

}
