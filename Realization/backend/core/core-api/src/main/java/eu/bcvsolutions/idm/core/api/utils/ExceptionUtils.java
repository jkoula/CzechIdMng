package eu.bcvsolutions.idm.core.api.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;

import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Exception utils
 *
 * @author svandav
 * @author Radek Tomiška
 */
public abstract class ExceptionUtils {
	
	/**
	 * If exception causal chain contains cause instance of ResultCodeException,
	 * then is return primary.
	 * 
	 * @param ex
	 * @return
	 */
	public static Throwable resolveException(Throwable ex) {
		Assert.notNull(ex, "Original exception is required to resolve ResultCodeException.");
		List<Throwable> causes = Throwables.getCausalChain(ex);
		// If is some cause instance of ResultCodeException, then we will use only it
		// (for better show on frontend)
		Throwable result = causes.stream()
				.filter(cause -> cause instanceof ResultCodeException)
				.findFirst().orElse(null);
		
		if(result != null) {
			return result;
		}
		
		// If ResultCodeException was not found, then we try to find CoreException
		result = causes.stream()
				.filter(cause -> cause instanceof CoreException)
				.findFirst().orElse(null);

		return result != null ? result : ex;
	}
	
	/**
	 * Logs exception with level defined by excepiton's result model.
	 * 
	 * @param logger
	 * @param ex
	 * @since 9.6.0
	 */
	public static void log(Logger logger, ResultCodeException ex) {
		Assert.notNull(logger, "Logger is required.");
		Assert.notNull(ex, "Exeption is required.");
		// error is required for ResultCodeException - one error will be defined
		ex
			.getError()
			.getErrors()
			.forEach(errorModel -> {
				// TODO: log source ex only the first time?
				log(logger, errorModel, ex);
			});
	}
	
	/**
	 * Logs exception with level defined by given result model.
	 * 
	 * @param logger
	 * @param resultModel
	 * @param ex
	 * @since 9.6.0
	 */
	public static void log(Logger logger, ResultModel resultModel, Throwable ex) {
		Assert.notNull(logger, "Logger is required.");
		// model not given - log exception only, if given
		if (resultModel == null) {
			if (ex != null) {
				logger.error("", ex);
			}
			return;
		}
		//
		if (resultModel.getStatus().is5xxServerError()) {
			logger.error(resultModel.toString(), ex);
		} else if(resultModel.getStatus().is2xxSuccessful()) {
			logger.debug(resultModel.toString(), ex);
		} else {
			logger.warn(resultModel.toString(), ex);
		}
	}
	
	/**
	 * Extracts a list of parameters according to the paramKey 
	 * from the list of ResultModel items(e.g. errors or infos list in {@link ResultModels} )
	 * Values in the result list are in the same order as records in ResultModels  
	 * 
	 * @param <T>
	 * @param paramKey
	 * @param resultList
	 * @return
	 */
	public static List<Object> getParameterChainByKey(String paramKey, List<ResultModel> resultList) {
		List<Object> paramList = resultList.stream().map(paramMap -> {
			return paramMap.getParameters().get(paramKey);
		}).filter(val -> {
			return val != null;
		}).collect(Collectors.toList());
		return paramList;
	}
	
}
