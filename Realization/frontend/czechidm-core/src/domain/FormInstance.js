import _ from 'lodash';
import Immutable from 'immutable';

/**
 * EAV form instance (definition + values)
 *
 * TODO: multi values
 */
export default class FormInstance {

  constructor(formDefinition, formValues = null) {
    this.definition = formDefinition;
    //
    // prepare attributes from given definition
    this.attributes = new Immutable.OrderedMap();
    if (formDefinition._embedded && formDefinition._embedded.formAttributes) {
      formDefinition._embedded.formAttributes.forEach(formAttribute => {
        this.attributes = this.attributes.set(formAttribute.name, formAttribute);
      });
    }
    //
    // prepare values
    this.values = new Immutable.OrderedMap();
    if (formValues) {
      formValues.forEach(formValue => {
        const attributeName = formValue._embedded.formAttribute.name;
        //
        const clonedFormValue = _.clone(formValue);
        // link to attribute definition
        clonedFormValue.formAttribute = this.getAttributeLink(clonedFormValue._embedded.formAttribute.name);
        if (!this.values.has(attributeName)) {
          this.values = this.values.set(attributeName, new Immutable.List());
        }
        this.values = this.values.set(attributeName, this.values.get(attributeName).push(clonedFormValue));
      });
    }
  }

  _clone() {
    return _.clone(this);
  }

  /**
   * Returns hateoas link to attribute defition by given attribute name
   *
   * @param  {string} attributeName
   * @return {string}
   */
  getAttributeLink(attributeName) {
    return `/form-attributes/${this.attributes.get(attributeName).id}`;
  }

  /**
   * Return form definition used for this instance
   *
   * @return {formDefinition}
   */
  getDefinition() {
    return this.definition;
  }

  /**
   * Return attribute definitions ordered by its seq
   *
   * @return {Immutable.OrderedMap} <attributeName, attribute>
   */
  getAttributes() {
    return this.attributes;
  }

  /**
   * Returns filled  attribute values (multivalues are oreded by its seq)
   *
   * @return {Immutable.OrderedMap} <attributeName, Immutable.List(formValue)>
   */
  getValues() {
    return this.values;
  }

  /**
   * Returns the first filled value of given attribute or null
   *
   * @param  {striung} attributeName
   * @return {formValue}
   */
  getSingleValue(attributeName) {
    if (!this.values.has(attributeName)) {
      return null;
    }
    return this.values.get(attributeName).first();
  }
}
